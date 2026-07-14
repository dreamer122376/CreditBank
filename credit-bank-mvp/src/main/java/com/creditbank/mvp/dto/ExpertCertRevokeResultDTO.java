package com.creditbank.mvp.dto;

import com.creditbank.mvp.entity.ExpertCert;

/** 撤销专家评审资质的结果：撤销后的记录 + 需要管理员注意的联动提示 */
public class ExpertCertRevokeResultDTO {

    private ExpertCert cert;

    /** 非空时提示管理员：该专家仍挂在该标准的审批流程节点上，需去流程管理调整 */
    private String warning;

    public ExpertCert getCert() {
        return cert;
    }

    public void setCert(ExpertCert cert) {
        this.cert = cert;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}
