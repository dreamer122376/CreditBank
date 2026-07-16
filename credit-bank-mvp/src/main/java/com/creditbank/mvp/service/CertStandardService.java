package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.CertAuditFlow;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.CertAuditFlowMapper;
import com.creditbank.mvp.mapper.CertStandardMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertStandardService {

    /** sys_user.role 中可用作认证适用对象的枚举 */
    private static final Set<String> VALID_TARGET_ROLES = new HashSet<>(Arrays.asList("student", "expert", "org_admin"));

    private final CertStandardMapper certStandardMapper;
    private final CertAuditFlowMapper certAuditFlowMapper;
    private final OrganizationMapper organizationMapper;
    private final SysUserMapper sysUserMapper;
    private final UserOpLogMapper userOpLogMapper;

    public CertStandardService(CertStandardMapper certStandardMapper,
                               CertAuditFlowMapper certAuditFlowMapper,
                               OrganizationMapper organizationMapper,
                               SysUserMapper sysUserMapper,
                               UserOpLogMapper userOpLogMapper) {
        this.certStandardMapper = certStandardMapper;
        this.certAuditFlowMapper = certAuditFlowMapper;
        this.organizationMapper = organizationMapper;
        this.sysUserMapper = sysUserMapper;
        this.userOpLogMapper = userOpLogMapper;
    }

    /** 查询全部认证标准（附带机构名、首节点审核人名、流程步数） */
    public List<CertStandard> list() {
        List<CertStandard> list = certStandardMapper.selectList(
                new LambdaQueryWrapper<CertStandard>().orderByDesc(CertStandard::getId));
        if (list.isEmpty()) {
            return list;
        }
        enrichDisplayFields(list);
        return list;
    }

    /** 按机构过滤：查询机构可见的认证标准（通用 + 本机构） */
    public List<CertStandard> listByOrg(Long orgId) {
        LambdaQueryWrapper<CertStandard> wrapper = new LambdaQueryWrapper<CertStandard>();
        if (orgId != null) {
            wrapper.and(w -> w.isNull(CertStandard::getOrgId).or().eq(CertStandard::getOrgId, orgId));
        }
        wrapper.orderByDesc(CertStandard::getId);
        List<CertStandard> list = certStandardMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return list;
        }
        enrichDisplayFields(list);
        return list;
    }

    public CertStandard getById(Long id) {
        CertStandard standard = certStandardMapper.selectById(id);
        if (standard == null) {
            throw new BizException("认证标准不存在：" + id);
        }
        enrichDisplayFields(Collections.singletonList(standard));
        return standard;
    }

    @Transactional(rollbackFor = Exception.class)
    public CertStandard create(CertStandard standard) {
        validateStandardFields(standard, true);
        standard.setId(null);
        if (standard.getIsEnabled() == null) {
            standard.setIsEnabled(1);
        }
        if (standard.getVersion() == null || standard.getVersion().trim().isEmpty()) {
            standard.setVersion("1.0");
        }
        if (standard.getNeedManualAudit() == null) {
            standard.setNeedManualAudit(1);
        }
        if (standard.getNeedManualAudit() == 0) {
            standard.setFirstNodeId(null);
        }
        certStandardMapper.insert(standard);
        CertStandard saved = certStandardMapper.selectById(standard.getId());
        SysUser operator = currentOperator();
        // 操作日志：创建证书标准
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_CERT_STANDARD, UserOpLog.ACTION_CREATE,
                    "创建证书标准：" + buildStandardSummary(saved)));
        }
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public CertStandard update(CertStandard standard) {
        CertStandard exist = certStandardMapper.selectById(standard.getId());
        if (exist == null) {
            throw new BizException("认证标准不存在：" + standard.getId());
        }
        validateStandardFields(standard, false);
        if (standard.getNeedManualAudit() != null && standard.getNeedManualAudit() == 0) {
            standard.setFirstNodeId(null);
        }
        certStandardMapper.updateById(standard);
        CertStandard saved = certStandardMapper.selectById(standard.getId());
        SysUser operator = currentOperator();
        // 操作日志：更新证书标准
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_CERT_STANDARD, UserOpLog.ACTION_UPDATE,
                    "更新证书标准：" + buildStandardSummary(saved)));
        }
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public CertStandard toggle(Long id) {
        CertStandard exist = certStandardMapper.selectById(id);
        if (exist == null) {
            throw new BizException("认证标准不存在：" + id);
        }
        boolean wasEnabled = exist.getIsEnabled() != null && exist.getIsEnabled() == 1;
        exist.setIsEnabled(wasEnabled ? 0 : 1);
        certStandardMapper.updateById(exist);
        SysUser operator = currentOperator();
        // 操作日志：启用/停用证书标准
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_CERT_STANDARD, UserOpLog.ACTION_RULE_TOGGLE,
                    (wasEnabled ? "停用证书标准：" : "启用证书标准：") + buildStandardSummary(exist)));
        }
        return exist;
    }

    // ---- 内部方法 ----

    private SysUser currentOperator() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return userId == null ? null : sysUserMapper.selectById(userId);
    }

    // 操作日志详情
    private String buildStandardSummary(CertStandard s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(s.getId());
        if (s.getStandardName() != null) {
            sb.append(" · 名称：").append(s.getStandardName());
        }
        if (s.getTargetRole() != null) {
            sb.append(" · 适用：").append(targetRoleLabel(s.getTargetRole()));
        }
        if (s.getVersion() != null) {
            sb.append(" · 版本：").append(s.getVersion());
        }
        if (s.getNeedManualAudit() != null) {
            sb.append(" · ").append(s.getNeedManualAudit() == 1 ? "人工审核" : "自动通过");
        }
        if (s.getIsEnabled() != null) {
            sb.append(" · ").append(s.getIsEnabled() == 1 ? "启用中" : "已停用");
        }
        return sb.toString();
    }

    private String targetRoleLabel(String role) {
        if ("student".equals(role)) return "学生";
        if ("expert".equals(role)) return "专家";
        if ("org_admin".equals(role)) return "机构管理员";
        return role == null ? "" : role;
    }

    private void validateStandardFields(CertStandard standard, boolean isCreate) {
        if (standard.getStandardName() == null || standard.getStandardName().trim().isEmpty()) {
            throw new BizException("认证名称不能为空");
        }
        if (standard.getTargetRole() == null || !VALID_TARGET_ROLES.contains(standard.getTargetRole())) {
            throw new BizException("适用对象非法，可选值：student / expert / org_admin");
        }
        if (standard.getNeedManualAudit() != null && standard.getNeedManualAudit() == 1) {
            if (isCreate) {
                // 新建时如果声明需要人工审核但还没有配流程，允许 first_node_id 为空（后续在流程管理页配置）
                // 但如果传了 first_node_id，必须校验节点存在且属于本标准
                if (standard.getFirstNodeId() != null) {
                    validateNodeBelongsToStandard(standard.getFirstNodeId(), null);
                }
            } else {
                if (standard.getFirstNodeId() != null) {
                    validateNodeBelongsToStandard(standard.getFirstNodeId(), standard.getId());
                }
            }
        }
    }

    private void validateNodeBelongsToStandard(Long nodeId, Long standardId) {
        CertAuditFlow node = certAuditFlowMapper.selectById(nodeId);
        if (node == null) {
            throw new BizException("流程节点不存在：" + nodeId);
        }
        if (standardId != null && !standardId.equals(node.getCertStandardId())) {
            throw new BizException("流程节点不属于该认证标准");
        }
    }

    /** 批量填充展示字段：机构名、首节点审核人名、流程步数 */
    private void enrichDisplayFields(List<CertStandard> list) {
        // 机构名
        Set<Long> orgIds = list.stream()
                .map(CertStandard::getOrgId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> orgNameMap = new HashMap<>();
        if (!orgIds.isEmpty()) {
            for (Organization o : organizationMapper.selectBatchIds(orgIds)) {
                orgNameMap.put(o.getId(), o.getName());
            }
        }

        // 首节点审核人名 + 流程步数
        Set<Long> firstNodeIds = list.stream()
                .map(CertStandard::getFirstNodeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, CertAuditFlow> nodeMap = new HashMap<>();
        if (!firstNodeIds.isEmpty()) {
            for (CertAuditFlow n : certAuditFlowMapper.selectBatchIds(firstNodeIds)) {
                nodeMap.put(n.getId(), n);
            }
        }

        // 审核人姓名
        Set<Long> auditorIds = nodeMap.values().stream()
                .map(CertAuditFlow::getAuditorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!auditorIds.isEmpty()) {
            for (SysUser u : sysUserMapper.selectBatchIds(auditorIds)) {
                userMap.put(u.getId(), u);
            }
        }

        // 每个标准的流程节点总数
        List<Long> standardIds = list.stream().map(CertStandard::getId).collect(Collectors.toList());
        Map<Long, Integer> stepCountMap = new HashMap<>();
        if (!standardIds.isEmpty()) {
            List<CertAuditFlow> allNodes = certAuditFlowMapper.selectList(
                    new LambdaQueryWrapper<CertAuditFlow>().in(CertAuditFlow::getCertStandardId, standardIds));
            for (CertAuditFlow n : allNodes) {
                stepCountMap.merge(n.getCertStandardId(), 1, Integer::sum);
            }
        }

        for (CertStandard s : list) {
            s.setOrgName(s.getOrgId() != null ? orgNameMap.getOrDefault(s.getOrgId(), "未知机构") : "平台通用");
            s.setFlowStepCount(stepCountMap.getOrDefault(s.getId(), 0));
            if (s.getFirstNodeId() != null) {
                CertAuditFlow firstNode = nodeMap.get(s.getFirstNodeId());
                if (firstNode != null) {
                    SysUser auditor = userMap.get(firstNode.getAuditorId());
                    s.setFirstAuditorName(auditor != null ? auditor.getRealName() : "未知用户");
                }
            }
        }
    }
}
