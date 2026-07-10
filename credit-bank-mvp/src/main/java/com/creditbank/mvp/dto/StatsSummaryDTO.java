package com.creditbank.mvp.dto;

public class StatsSummaryDTO {
    private Long totalUsers;
    private Long totalOrgs;
    private Long pendingCount;
    private Long totalCredit;

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
}
