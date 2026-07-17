package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.StudentCertVerifyDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.CertAuditFlow;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.StudentCert;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.ApplicationMapper;
import com.creditbank.mvp.mapper.CertAuditFlowMapper;
import com.creditbank.mvp.mapper.CertStandardMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.StudentCertMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import com.creditbank.mvp.util.CurrentUserUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class StudentCertService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final DateTimeFormatter CERT_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StudentCertMapper studentCertMapper;
    private final ApplicationMapper applicationMapper;
    private final SysUserMapper sysUserMapper;
    private final CertStandardMapper certStandardMapper;
    private final OrganizationMapper organizationMapper;
    private final CertAuditFlowMapper certAuditFlowMapper;
    private final UserOpLogMapper userOpLogMapper;

    public StudentCertService(StudentCertMapper studentCertMapper,
                              ApplicationMapper applicationMapper,
                              SysUserMapper sysUserMapper,
                              CertStandardMapper certStandardMapper,
                              OrganizationMapper organizationMapper,
                              CertAuditFlowMapper certAuditFlowMapper,
                              UserOpLogMapper userOpLogMapper) {
        this.studentCertMapper = studentCertMapper;
        this.applicationMapper = applicationMapper;
        this.sysUserMapper = sysUserMapper;
        this.certStandardMapper = certStandardMapper;
        this.organizationMapper = organizationMapper;
        this.certAuditFlowMapper = certAuditFlowMapper;
        this.userOpLogMapper = userOpLogMapper;
    }

    public StudentCert issueForApplication(Application app) {
        if (app == null || !"CERT_APPLY".equals(app.getBizType())) {
            throw new BizException("只能为学生证书认证申请发证");
        }
        Long standardId = app.getBizKey();
        if (standardId == null) {
            standardId = readStandardId(app.getFormData());
        }
        if (standardId == null) {
            throw new BizException("申请数据缺少认证标准，无法发证");
        }

        StudentCert existing = studentCertMapper.selectOne(
                new LambdaQueryWrapper<StudentCert>()
                        .eq(StudentCert::getStudentId, app.getApplicantId())
                        .eq(StudentCert::getCertStandardId, standardId)
                        .eq(StudentCert::getStatus, 1)
                        .last("LIMIT 1"));
        if (existing != null) {
            // 幂等返回，不重复写日志
            return existing;
        }

        SysUser student = sysUserMapper.selectById(app.getApplicantId());
        if (student == null || !"student".equals(student.getRole())) {
            throw new BizException("申请人不是有效学生账号，无法发证");
        }
        CertStandard standard = certStandardMapper.selectById(standardId);
        if (standard == null) {
            throw new BizException("认证标准不存在，无法发证：" + standardId);
        }

        LocalDateTime now = LocalDateTime.now();
        StudentCert cert = new StudentCert();
        cert.setStudentId(student.getId());
        cert.setCertStandardId(standardId);
        cert.setApplicationId(app.getId());
        cert.setCertNo(buildCertNo(now, student.getId(), app.getId()));
        cert.setStudentName(student.getRealName());
        cert.setCertName(standard.getStandardName());
        cert.setOrgName(resolveOrgName(standard, student));
        cert.setVerifyCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        cert.setStatus(1);
        cert.setIssuedAt(now);
        cert.setValidUntil(now.plusYears(1));
        studentCertMapper.insert(cert);
        StudentCert saved = studentCertMapper.selectById(cert.getId());

        // 发证操作日志：operator = 当前登录的审核人（ThreadLocal 中），target = 学生
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        SysUser operator = operatorId == null ? null : sysUserMapper.selectById(operatorId);
        userOpLogMapper.insert(UserOpLog.createLog(
                operator == null ? operatorId : operator.getId(),
                operator == null ? null : operator.getRealName(),
                student.getId(), student.getRealName(),
                UserOpLog.MODULE_STUDENT_CERT, UserOpLog.ACTION_ISSUE,
                "发放学生证书：" + buildCertSummary(saved)));
        return saved;
    }

    public List<StudentCert> listByStudent(Long studentId, String role, Long userId) {
        if (!"admin".equals(role) && (userId == null || !userId.equals(studentId))) {
            throw new BizException("无权查看该学生的证书");
        }
        ensureIssuedForApprovedApplications(studentId);
        return studentCertMapper.selectList(
                new LambdaQueryWrapper<StudentCert>()
                        .eq(StudentCert::getStudentId, studentId)
                        .eq(StudentCert::getStatus, 1)
                        .orderByDesc(StudentCert::getIssuedAt));
    }

    public StudentCert getById(Long id, String role, Long userId) {
        StudentCert cert = studentCertMapper.selectById(id);
        if (cert == null || cert.getStatus() == null || cert.getStatus() != 1) {
            throw new BizException("证书不存在或已失效：" + id);
        }
        ensureCanViewCert(cert, role, userId);
        return cert;
    }

    public StudentCert getByApplication(Long applicationId, String role, Long userId) {
        StudentCert cert = studentCertMapper.selectOne(
                new LambdaQueryWrapper<StudentCert>()
                        .eq(StudentCert::getApplicationId, applicationId)
                        .last("LIMIT 1"));
        if (cert == null) {
            throw new BizException("该申请尚未生成证书");
        }
        ensureCanViewCert(cert, role, userId);
        return cert;
    }

    /**
     * 作废证书（仅系统管理员或所属机构管理员可操作）
     */
    public StudentCert revoke(Long id, String role, Long userId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BizException("作废证书必须填写原因");
        }
        StudentCert cert = studentCertMapper.selectById(id);
        if (cert == null) {
            throw new BizException("证书不存在：" + id);
        }
        if (cert.getStatus() != null && cert.getStatus() == 0) {
            throw new BizException("证书已作废，无需重复操作");
        }
        ensureCanRevokeCert(cert, role, userId);
        cert.setStatus(0);
        cert.setRevokeReason(reason.trim());
        cert.setRevokedAt(LocalDateTime.now());
        studentCertMapper.updateById(cert);
        StudentCert saved = studentCertMapper.selectById(id);

        // 撤回操作日志：操作人 userId 从参数取，target = 证书学生
        SysUser operator = userId == null ? null : sysUserMapper.selectById(userId);
        userOpLogMapper.insert(UserOpLog.createLog(
                operator == null ? userId : operator.getId(),
                operator == null ? null : operator.getRealName(),
                cert.getStudentId(), cert.getStudentName(),
                UserOpLog.MODULE_STUDENT_CERT, UserOpLog.ACTION_REVOKE,
                "作废学生证书：" + buildCertSummary(saved) + " · 原因：" + reason.trim()));
        return saved;
    }

    public StudentCertVerifyDTO verify(String certNo, String verifyCode) {
        if (certNo == null || certNo.trim().isEmpty()
                || verifyCode == null || verifyCode.trim().isEmpty()) {
            throw new BizException("请输入证书编号和核验码");
        }
        StudentCert cert = studentCertMapper.selectOne(
                new LambdaQueryWrapper<StudentCert>()
                        .eq(StudentCert::getCertNo, certNo.trim())
                        .eq(StudentCert::getVerifyCode, verifyCode.trim())
                        .last("LIMIT 1"));
        if (cert == null) {
            throw new BizException("证书不存在或核验码错误");
        }

        StudentCertVerifyDTO dto = new StudentCertVerifyDTO();
        dto.setCertNo(cert.getCertNo());
        dto.setStudentName(cert.getStudentName());
        dto.setCertName(cert.getCertName());
        dto.setOrgName(cert.getOrgName());
        dto.setIssuedAt(cert.getIssuedAt());
        dto.setValidUntil(cert.getValidUntil());
        dto.setRevokedAt(cert.getRevokedAt());

        if (cert.getStatus() == null || cert.getStatus() != 1) {
            dto.setValid(false);
            dto.setStatus("REVOKED");
            dto.setMessage("该证书已作废");
            return dto;
        }
        if (cert.getValidUntil() != null && cert.getValidUntil().isBefore(LocalDateTime.now())) {
            dto.setValid(false);
            dto.setStatus("EXPIRED");
            dto.setMessage("该证书已过有效期");
            return dto;
        }
        dto.setValid(true);
        dto.setStatus("VALID");
        dto.setMessage("证书真实有效");
        return dto;
    }

    private void ensureIssuedForApprovedApplications(Long studentId) {
        List<Application> approvedApps = applicationMapper.selectList(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getBizType, "CERT_APPLY")
                        .eq(Application::getApplicantId, studentId)
                        .eq(Application::getCurrentStatus, 2));
        for (Application app : approvedApps) {
            issueForApplication(app);
        }
    }

    private void ensureCanViewCert(StudentCert cert, String role, Long userId) {
        if ("admin".equals(role)) {
            return;
        }
        if ("student".equals(role) && userId != null && userId.equals(cert.getStudentId())) {
            return;
        }
        Application app = cert.getApplicationId() == null ? null : applicationMapper.selectById(cert.getApplicationId());
        SysUser user = userId == null ? null : sysUserMapper.selectById(userId);
        if ("org_admin".equals(role) && app != null && user != null && user.getOrgId() != null) {
            Long orgId = user.getOrgId();
            SysUser applicant = sysUserMapper.selectById(app.getApplicantId());
            if (applicant != null && orgId.equals(applicant.getOrgId())) {
                return;
            }
            CertStandard standard = cert.getCertStandardId() != null ? certStandardMapper.selectById(cert.getCertStandardId()) : null;
            if (standard == null && app != null && app.getBizKey() != null) {
                standard = certStandardMapper.selectById(app.getBizKey());
            }
            if (standard != null && orgId.equals(standard.getOrgId())) {
                return;
            }
        }
        if ("expert".equals(role) && app != null && userId != null && app.getCurrentNodeId() != null) {
            CertAuditFlow node = certAuditFlowMapper.selectById(app.getCurrentNodeId());
            if (node != null && userId.equals(node.getAuditorId())) {
                return;
            }
        }
        throw new BizException("无权查看该证书");
    }

    private void ensureCanRevokeCert(StudentCert cert, String role, Long userId) {
        if ("admin".equals(role)) {
            return;
        }
        Application app = cert.getApplicationId() == null ? null : applicationMapper.selectById(cert.getApplicationId());
        SysUser user = userId == null ? null : sysUserMapper.selectById(userId);
        if ("org_admin".equals(role) && app != null && user != null && user.getOrgId() != null) {
            Long orgId = user.getOrgId();
            SysUser applicant = sysUserMapper.selectById(app.getApplicantId());
            if (applicant != null && orgId.equals(applicant.getOrgId())) {
                return;
            }
            CertStandard standard = cert.getCertStandardId() != null ? certStandardMapper.selectById(cert.getCertStandardId()) : null;
            if (standard == null && app != null && app.getBizKey() != null) {
                standard = certStandardMapper.selectById(app.getBizKey());
            }
            if (standard != null && orgId.equals(standard.getOrgId())) {
                return;
            }
        }
        throw new BizException("只有系统管理员或所属机构管理员可以作废证书");
    }

    private String resolveOrgName(CertStandard standard, SysUser student) {
        Long orgId = standard.getOrgId() != null ? standard.getOrgId() : student.getOrgId();
        if (orgId != null) {
            Organization org = organizationMapper.selectById(orgId);
            if (org != null && org.getName() != null && !org.getName().trim().isEmpty()) {
                return org.getName();
            }
        }
        return "学分银行平台";
    }

    private String buildCertNo(LocalDateTime issuedAt, Long studentId, Long applicationId) {
        return String.format("CB-%s-%04d-%04d", issuedAt.format(CERT_NO_DATE), studentId, applicationId);
    }

    // 操作日志详情
    private String buildCertSummary(StudentCert cert) {
        if (cert == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(cert.getId());
        if (cert.getCertNo() != null) {
            sb.append(" · 编号：").append(cert.getCertNo());
        }
        if (cert.getCertName() != null) {
            sb.append(" · 证书：").append(cert.getCertName());
        }
        if (cert.getStudentName() != null) {
            sb.append(" · 学生：").append(cert.getStudentName());
        }
        if (cert.getOrgName() != null && !cert.getOrgName().isEmpty()) {
            sb.append(" · 机构：").append(cert.getOrgName());
        }
        if (cert.getStatus() != null) {
            sb.append(" · ").append(cert.getStatus() == 1 ? "有效" : "已作废");
        }
        return sb.toString();
    }

    private Long readStandardId(String formData) {
        Long id = readLong(formData, "certStandardId");
        return id != null ? id : readLong(formData, "standardId");
    }

    private Long readLong(String formData, String field) {
        if (formData == null || formData.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode node = JSON.readTree(formData);
            return node.hasNonNull(field) ? node.get(field).asLong() : null;
        } catch (Exception e) {
            throw new BizException("申请表单数据不是合法的 JSON");
        }
    }
}
