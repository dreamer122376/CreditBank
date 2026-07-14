package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.ExpertCertRevokeResultDTO;
import com.creditbank.mvp.entity.CertAuditFlow;
import com.creditbank.mvp.entity.ExpertCert;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.CertAuditFlowMapper;
import com.creditbank.mvp.mapper.ExpertCertMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpertCertService {

    /** 评审资质有效期（年），到期后需重新申请认证 */
    public static final int VALID_YEARS = 3;

    private final ExpertCertMapper expertCertMapper;
    private final SysUserMapper sysUserMapper;
    private final CertAuditFlowMapper certAuditFlowMapper;

    public ExpertCertService(ExpertCertMapper expertCertMapper,
                             SysUserMapper sysUserMapper,
                             CertAuditFlowMapper certAuditFlowMapper) {
        this.expertCertMapper = expertCertMapper;
        this.sysUserMapper = sysUserMapper;
        this.certAuditFlowMapper = certAuditFlowMapper;
    }

    /** 有效 = status=1 且未过有效期（valid_until 为 NULL 视为长期） */
    private LambdaQueryWrapper<ExpertCert> validWrapper() {
        LocalDateTime now = LocalDateTime.now();
        return new LambdaQueryWrapper<ExpertCert>()
                .eq(ExpertCert::getStatus, 1)
                .and(w -> w.isNull(ExpertCert::getValidUntil)
                        .or().gt(ExpertCert::getValidUntil, now));
    }

    /** 全部有效认证记录（专家列表页做徽章用，前端按 expertId 分组） */
    public List<ExpertCert> listValid() {
        return expertCertMapper.selectList(validWrapper());
    }

    /**
     * 某个专家的全部认证记录（含已过期/已撤销），资料页与资质管理用，
     * 有效性由前端按 status/validUntil 判断。
     */
    public List<ExpertCert> listByExpert(Long expertId) {
        return expertCertMapper.selectList(
                new LambdaQueryWrapper<ExpertCert>()
                        .eq(ExpertCert::getExpertId, expertId)
                        .orderByDesc(ExpertCert::getIssuedAt));
    }

    /** 持有某认证标准有效评审资质的专家名单（配置审批流程时专家候选人的数据源） */
    public List<SysUser> listCertifiedExperts(Long certStandardId) {
        List<ExpertCert> certs = expertCertMapper.selectList(
                validWrapper().eq(ExpertCert::getCertStandardId, certStandardId));
        List<Long> expertIds = certs.stream()
                .map(ExpertCert::getExpertId)
                .distinct()
                .collect(Collectors.toList());
        if (expertIds.isEmpty()) {
            return new ArrayList<>();
        }
        return sysUserMapper.selectBatchIds(expertIds).stream()
                .filter(u -> u.getStatus() != null && u.getStatus() == 1)
                .collect(Collectors.toList());
    }

    /** 该专家是否持有该认证标准的有效资质（提交申请去重、流程配置校验共用） */
    public boolean hasCert(Long expertId, Long certStandardId) {
        return expertCertMapper.selectCount(
                validWrapper()
                        .eq(ExpertCert::getExpertId, expertId)
                        .eq(ExpertCert::getCertStandardId, certStandardId)) > 0;
    }

    /**
     * 撤销评审资质（仅系统管理员）。资质记录保留可追溯；
     * 若该专家仍挂在该标准的审批流程节点上，返回 warning 提醒管理员调整流程。
     */
    @Transactional(rollbackFor = Exception.class)
    public ExpertCertRevokeResultDTO revoke(Long id, String role, String reason) {
        if (!"admin".equals(role)) {
            throw new BizException("只有系统管理员可以撤销专家评审资质");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BizException("撤销资质必须填写原因");
        }
        ExpertCert cert = expertCertMapper.selectById(id);
        if (cert == null) {
            throw new BizException("资质记录不存在：" + id);
        }
        if (cert.getStatus() != null && cert.getStatus() == 0) {
            throw new BizException("该资质已被撤销，无需重复操作");
        }
        cert.setStatus(0);
        cert.setRevokeReason(reason.trim());
        cert.setRevokedAt(LocalDateTime.now());
        expertCertMapper.updateById(cert);

        syncExpertField(cert.getExpertId());

        ExpertCertRevokeResultDTO result = new ExpertCertRevokeResultDTO();
        result.setCert(expertCertMapper.selectById(id));

        Long onFlowNodes = certAuditFlowMapper.selectCount(
                new LambdaQueryWrapper<CertAuditFlow>()
                        .eq(CertAuditFlow::getAuditorId, cert.getExpertId())
                        .eq(CertAuditFlow::getCertStandardId, cert.getCertStandardId()));
        if (onFlowNodes != null && onFlowNodes > 0) {
            SysUser expert = sysUserMapper.selectById(cert.getExpertId());
            String name = expert != null ? expert.getRealName() : ("专家#" + cert.getExpertId());
            result.setWarning("注意：" + name + " 仍是该认证标准审批流程中的审核人，"
                    + "请到「认证标准 → 流程管理」中调整审批链，避免无资质人员继续参与评审。");
        }
        return result;
    }

    /**
     * 按当前有效资质同步专家的"擅长领域"展示字段（多个领域用顿号连接）。
     * 无任何有效资质时不清空，保留管理员手工维护的值。
     */
    public void syncExpertField(Long expertId) {
        List<ExpertCert> validCerts = expertCertMapper.selectList(
                validWrapper().eq(ExpertCert::getExpertId, expertId)
                        .orderByAsc(ExpertCert::getIssuedAt));
        if (validCerts.isEmpty()) {
            return;
        }
        String joined = validCerts.stream()
                .map(ExpertCert::getFieldName)
                .filter(n -> n != null && !n.trim().isEmpty())
                .distinct()
                .collect(Collectors.joining("、"));
        if (joined.isEmpty()) {
            return;
        }
        SysUser expert = sysUserMapper.selectById(expertId);
        if (expert != null) {
            expert.setExpertField(joined.length() > 100 ? joined.substring(0, 100) : joined);
            sysUserMapper.updateById(expert);
        }
    }
}
