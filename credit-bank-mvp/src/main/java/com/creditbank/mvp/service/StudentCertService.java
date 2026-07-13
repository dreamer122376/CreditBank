package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.StudentCert;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.CertStandardMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.ApplicationMapper;
import com.creditbank.mvp.mapper.StudentCertMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
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

    public StudentCertService(StudentCertMapper studentCertMapper,
                              ApplicationMapper applicationMapper,
                              SysUserMapper sysUserMapper,
                              CertStandardMapper certStandardMapper,
                              OrganizationMapper organizationMapper) {
        this.studentCertMapper = studentCertMapper;
        this.applicationMapper = applicationMapper;
        this.sysUserMapper = sysUserMapper;
        this.certStandardMapper = certStandardMapper;
        this.organizationMapper = organizationMapper;
    }

    /** 审批终审通过后发证。重复调用保持幂等，避免重复发同一张证。 */
    public StudentCert issueForApplication(Application app) {
        if (app == null || !"CERT_APPLY".equals(app.getBizType())) {
            throw new BizException("只能为学生证书认证申请发证");
        }
        Long standardId = readStandardId(app.getFormData());
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
        return studentCertMapper.selectById(cert.getId());
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
        if (!"admin".equals(role) && (userId == null || !userId.equals(cert.getStudentId()))) {
            throw new BizException("无权查看该证书");
        }
        return cert;
    }

    private void ensureIssuedForApprovedApplications(Long studentId) {
        List<Application> approvedApps = applicationMapper.selectList(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getBizType, "CERT_APPLY")
                        .eq(Application::getApplicantId, studentId)
                        .eq(Application::getCurrentStatus, 3));
        for (Application app : approvedApps) {
            issueForApplication(app);
        }
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
