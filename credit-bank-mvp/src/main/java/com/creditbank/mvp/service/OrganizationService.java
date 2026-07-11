package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.mapper.OrganizationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {

    /** 机构状态：0待审核，1启用，2禁用 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 2;

    private final OrganizationMapper organizationMapper;

    public OrganizationService(OrganizationMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    public List<Organization> list() {
        return organizationMapper.selectList(
                new LambdaQueryWrapper<Organization>().orderByDesc(Organization::getId));
    }

    public Organization create(Organization org) {
        if (org.getName() == null || org.getName().trim().isEmpty()) {
            throw new BizException("机构名称不能为空");
        }
        org.setId(null);
        org.setStatus(STATUS_PENDING);
        organizationMapper.insert(org);
        return organizationMapper.selectById(org.getId());
    }

    public Organization update(Organization org) {
        Organization exist = organizationMapper.selectById(org.getId());
        if (exist == null) {
            throw new BizException("机构不存在：" + org.getId());
        }
        organizationMapper.updateById(org);
        return organizationMapper.selectById(org.getId());
    }

    /** 入驻审核 / 启停：只允许流转到 1启用 或 2禁用 */
    public Organization changeStatus(Long id, Integer status) {
        Organization exist = organizationMapper.selectById(id);
        if (exist == null) {
            throw new BizException("机构不存在：" + id);
        }
        if (status == null || (status != STATUS_ENABLED && status != STATUS_DISABLED)) {
            throw new BizException("非法的机构状态：" + status);
        }
        exist.setStatus(status);
        organizationMapper.updateById(exist);
        return exist;
    }
}
