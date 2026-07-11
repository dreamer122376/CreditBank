package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.mapper.CertStandardMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertStandardService {

    private final CertStandardMapper certStandardMapper;

    public CertStandardService(CertStandardMapper certStandardMapper) {
        this.certStandardMapper = certStandardMapper;
    }

    public List<CertStandard> list() {
        return certStandardMapper.selectList(
                new LambdaQueryWrapper<CertStandard>().orderByDesc(CertStandard::getId));
    }

    public CertStandard create(CertStandard standard) {
        if (standard.getStandardName() == null || standard.getStandardName().trim().isEmpty()) {
            throw new BizException("认证名称不能为空");
        }
        if (standard.getMinCredit() == null || standard.getMinCredit() <= 0) {
            throw new BizException("最低积分必须为正数");
        }
        standard.setId(null);
        if (standard.getIsEnabled() == null) {
            standard.setIsEnabled(1);
        }
        certStandardMapper.insert(standard);
        return certStandardMapper.selectById(standard.getId());
    }

    public CertStandard update(CertStandard standard) {
        CertStandard exist = certStandardMapper.selectById(standard.getId());
        if (exist == null) {
            throw new BizException("认证标准不存在：" + standard.getId());
        }
        if (standard.getMinCredit() != null && standard.getMinCredit() <= 0) {
            throw new BizException("最低积分必须为正数");
        }
        certStandardMapper.updateById(standard);
        return certStandardMapper.selectById(standard.getId());
    }

    public CertStandard toggle(Long id) {
        CertStandard exist = certStandardMapper.selectById(id);
        if (exist == null) {
            throw new BizException("认证标准不存在：" + id);
        }
        exist.setIsEnabled(exist.getIsEnabled() != null && exist.getIsEnabled() == 1 ? 0 : 1);
        certStandardMapper.updateById(exist);
        return exist;
    }
}
