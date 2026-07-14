package com.creditbank.mvp.dto;

import com.creditbank.mvp.entity.Organization;

/**
 * 机构审核结果，包含生成的机构管理员账号信息。
 */
public class OrganizationAuditResult {

    private Organization organization;
    private boolean created;
    private String adminUsername;
    private String adminPassword;
    private String message;

    public OrganizationAuditResult() {
    }

    public static OrganizationAuditResult of(Organization organization, boolean created,
                                              String adminUsername, String adminPassword,
                                              String message) {
        OrganizationAuditResult result = new OrganizationAuditResult();
        result.setOrganization(organization);
        result.setCreated(created);
        result.setAdminUsername(adminUsername);
        result.setAdminPassword(adminPassword);
        result.setMessage(message);
        return result;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
