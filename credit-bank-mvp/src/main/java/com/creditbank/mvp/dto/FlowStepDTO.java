package com.creditbank.mvp.dto;

/** 认证审批链中的一步（业务流程详情页展示用） */
public class FlowStepDTO {

    private Integer stepNo;
    private Long nodeId;
    private String auditorName;
    /** done=已通过该步 / current=当前停留 / pending=未到达 / rejected=在该步被驳回 */
    private String state;

    public Integer getStepNo() {
        return stepNo;
    }

    public void setStepNo(Integer stepNo) {
        this.stepNo = stepNo;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getAuditorName() {
        return auditorName;
    }

    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
