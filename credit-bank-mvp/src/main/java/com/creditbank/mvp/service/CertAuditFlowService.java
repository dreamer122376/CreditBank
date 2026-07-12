package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.CertAuditFlow;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.CertAuditFlowMapper;
import com.creditbank.mvp.mapper.CertStandardMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertAuditFlowService {

    /** 单个标准最多允许的流程节点数，防止链表过长 */
    private static final int MAX_NODES = 20;

    private final CertAuditFlowMapper certAuditFlowMapper;
    private final CertStandardMapper certStandardMapper;
    private final SysUserMapper sysUserMapper;

    public CertAuditFlowService(CertAuditFlowMapper certAuditFlowMapper,
                                CertStandardMapper certStandardMapper,
                                SysUserMapper sysUserMapper) {
        this.certAuditFlowMapper = certAuditFlowMapper;
        this.certStandardMapper = certStandardMapper;
        this.sysUserMapper = sysUserMapper;
    }

    /** 查询某认证标准的完整审批链（按链表顺序排列） */
    public List<CertAuditFlow> listByStandard(Long certStandardId) {
        CertStandard standard = certStandardMapper.selectById(certStandardId);
        if (standard == null) {
            throw new BizException("认证标准不存在：" + certStandardId);
        }
        List<CertAuditFlow> allNodes = certAuditFlowMapper.selectList(
                new LambdaQueryWrapper<CertAuditFlow>()
                        .eq(CertAuditFlow::getCertStandardId, certStandardId)
                        .orderByAsc(CertAuditFlow::getId));
        if (allNodes.isEmpty()) {
            return allNodes;
        }
        // 按链表顺序重排：从 first_node_id 开始沿 next_node_id 走
        List<CertAuditFlow> ordered = new ArrayList<>();
        Map<Long, CertAuditFlow> idMap = allNodes.stream()
                .collect(Collectors.toMap(CertAuditFlow::getId, n -> n));
        Long currentId = standard.getFirstNodeId();
        Set<Long> visited = new HashSet<>();
        while (currentId != null) {
            if (visited.contains(currentId)) {
                throw new BizException("审批流程存在环，请联系管理员修复数据");
            }
            visited.add(currentId);
            CertAuditFlow node = idMap.get(currentId);
            if (node == null) {
                break;
            }
            ordered.add(node);
            currentId = node.getNextNodeId();
        }
        enrichDisplayFields(ordered);
        return ordered;
    }

    /** 批量创建/重建某标准的审批流程（整体替换） */
    @Transactional(rollbackFor = Exception.class)
    public List<CertAuditFlow> saveFlow(Long certStandardId, List<CertAuditFlow> nodes) {
        CertStandard standard = certStandardMapper.selectById(certStandardId);
        if (standard == null) {
            throw new BizException("认证标准不存在：" + certStandardId);
        }
        if (nodes == null || nodes.isEmpty()) {
            throw new BizException("审批流程至少需要一个节点");
        }
        if (nodes.size() > MAX_NODES) {
            throw new BizException("审批流程节点数不能超过 " + MAX_NODES);
        }

        // 校验审核人
        Set<Long> auditorIds = nodes.stream()
                .map(CertAuditFlow::getAuditorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (auditorIds.size() != nodes.size()) {
            throw new BizException("每个流程节点必须指定审核人");
        }
        for (SysUser auditor : sysUserMapper.selectBatchIds(auditorIds)) {
            if (auditor.getStatus() == null || auditor.getStatus() == 0) {
                throw new BizException("审核人 " + auditor.getRealName() + " 已被冻结，不能作为审批人");
            }
        }

        // 先删除旧节点
        certAuditFlowMapper.delete(new LambdaQueryWrapper<CertAuditFlow>()
                .eq(CertAuditFlow::getCertStandardId, certStandardId));

        // 依次插入新节点，构建链表
        Long prevNodeId = null;
        List<CertAuditFlow> inserted = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            CertAuditFlow node = nodes.get(i);
            node.setId(null);
            node.setCertStandardId(certStandardId);
            // 最后一个节点的 next_node_id 为 null
            node.setNextNodeId(null);
            certAuditFlowMapper.insert(node);
            Long currentNodeId = node.getId();
            if (prevNodeId != null) {
                CertAuditFlow prev = new CertAuditFlow();
                prev.setId(prevNodeId);
                prev.setNextNodeId(currentNodeId);
                certAuditFlowMapper.updateById(prev);
            }
            prevNodeId = currentNodeId;
            inserted.add(node);
        }

        // 更新认证标准的 first_node_id
        Long firstNodeId = inserted.get(0).getId();
        CertStandard update = new CertStandard();
        update.setId(certStandardId);
        update.setFirstNodeId(firstNodeId);
        certStandardMapper.updateById(update);

        enrichDisplayFields(inserted);
        return inserted;
    }

    /** 删除某标准的整个审批流程 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFlow(Long certStandardId) {
        certAuditFlowMapper.delete(new LambdaQueryWrapper<CertAuditFlow>()
                .eq(CertAuditFlow::getCertStandardId, certStandardId));
        // 清空标准的 first_node_id
        CertStandard update = new CertStandard();
        update.setId(certStandardId);
        update.setFirstNodeId(null);
        certStandardMapper.updateById(update);
    }

    // ---- 内部方法 ----

    private void enrichDisplayFields(List<CertAuditFlow> nodes) {
        if (nodes.isEmpty()) {
            return;
        }
        Set<Long> auditorIds = nodes.stream()
                .map(CertAuditFlow::getAuditorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!auditorIds.isEmpty()) {
            for (SysUser u : sysUserMapper.selectBatchIds(auditorIds)) {
                userMap.put(u.getId(), u);
            }
        }
        for (CertAuditFlow node : nodes) {
            SysUser auditor = userMap.get(node.getAuditorId());
            if (auditor != null) {
                node.setAuditorName(auditor.getRealName());
                node.setAuditorRole(auditor.getRole());
            }
            node.setIsFinal(node.getNextNodeId() == null);
        }
    }
}
