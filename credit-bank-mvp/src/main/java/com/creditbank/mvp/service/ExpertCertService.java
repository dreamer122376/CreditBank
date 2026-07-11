package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.entity.ExpertCert;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.ExpertCertMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpertCertService {

    private final ExpertCertMapper expertCertMapper;
    private final SysUserMapper sysUserMapper;

    public ExpertCertService(ExpertCertMapper expertCertMapper, SysUserMapper sysUserMapper) {
        this.expertCertMapper = expertCertMapper;
        this.sysUserMapper = sysUserMapper;
    }

    /** 全部有效认证记录（专家列表页做徽章用，前端按 expertId 分组） */
    public List<ExpertCert> listValid() {
        return expertCertMapper.selectList(
                new LambdaQueryWrapper<ExpertCert>().eq(ExpertCert::getStatus, 1));
    }

    /** 某个专家的有效认证（资料页徽章墙） */
    public List<ExpertCert> listByExpert(Long expertId) {
        return expertCertMapper.selectList(
                new LambdaQueryWrapper<ExpertCert>()
                        .eq(ExpertCert::getExpertId, expertId)
                        .eq(ExpertCert::getStatus, 1)
                        .orderByDesc(ExpertCert::getIssuedAt));
    }

    /** 持有某认证标准评审资质的专家名单（指派下拉的数据源） */
    public List<SysUser> listCertifiedExperts(Long certStandardId) {
        List<ExpertCert> certs = expertCertMapper.selectList(
                new LambdaQueryWrapper<ExpertCert>()
                        .eq(ExpertCert::getCertStandardId, certStandardId)
                        .eq(ExpertCert::getStatus, 1));
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

    /** 指派校验用：该专家是否持有该认证标准的有效资质 */
    public boolean hasCert(Long expertId, Long certStandardId) {
        return expertCertMapper.selectCount(
                new LambdaQueryWrapper<ExpertCert>()
                        .eq(ExpertCert::getExpertId, expertId)
                        .eq(ExpertCert::getCertStandardId, certStandardId)
                        .eq(ExpertCert::getStatus, 1)) > 0;
    }
}
