package com.creditbank.mvp.dto;

public class StatsSummaryDTO {
    private Long totalUsers;
    private Long totalOrgs;
    private Long pendingCount;
    private Long totalCredit;

    // 扩展字段
    private Long rewardCount;
    private Long orgStudentCount;
    private Long orgProjectCount;
    private Long joinedProjectCount;
    private Integer monthPointChange;
    private Long reviewedCount;

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalOrgs() {
        return totalOrgs;
    }

    public void setTotalOrgs(Long totalOrgs) {
        this.totalOrgs = totalOrgs;
    }

    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Long getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(Long totalCredit) {
        this.totalCredit = totalCredit;
    }

    public Long getRewardCount() {
        return rewardCount;
    }

    public void setRewardCount(Long rewardCount) {
        this.rewardCount = rewardCount;
    }

    public Long getOrgStudentCount() {
        return orgStudentCount;
    }

    public void setOrgStudentCount(Long orgStudentCount) {
        this.orgStudentCount = orgStudentCount;
    }

    public Long getOrgProjectCount() {
        return orgProjectCount;
    }

    public void setOrgProjectCount(Long orgProjectCount) {
        this.orgProjectCount = orgProjectCount;
    }

    public Long getJoinedProjectCount() {
        return joinedProjectCount;
    }

    public void setJoinedProjectCount(Long joinedProjectCount) {
        this.joinedProjectCount = joinedProjectCount;
    }

    public Integer getMonthPointChange() {
        return monthPointChange;
    }

    public void setMonthPointChange(Integer monthPointChange) {
        this.monthPointChange = monthPointChange;
    }

    public Long getReviewedCount() {
        return reviewedCount;
    }

    public void setReviewedCount(Long reviewedCount) {
        this.reviewedCount = reviewedCount;
    }
}
