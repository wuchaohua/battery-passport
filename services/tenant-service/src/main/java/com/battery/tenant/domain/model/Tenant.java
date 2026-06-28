 package com.battery.tenant.domain.model;

 import com.baomidou.mybatisplus.annotation.TableName;
 import com.battery.mybatis.BaseEntity;
 import java.time.LocalDate;

 @TableName("sys_tenant")
 public class Tenant extends BaseEntity {
     private String tenantCode;
     private String tenantName;
     private String contactName;
     private String contactPhone;
     private String contactEmail;
     private String plan;
     private LocalDate expireDate;
     private String status;

     public String getTenantCode() { return tenantCode; }
     public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
     public String getTenantName() { return tenantName; }
     public void setTenantName(String tenantName) { this.tenantName = tenantName; }
     public String getContactName() { return contactName; }
     public void setContactName(String contactName) { this.contactName = contactName; }
     public String getContactPhone() { return contactPhone; }
     public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
     public String getContactEmail() { return contactEmail; }
     public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
     public String getPlan() { return plan; }
     public void setPlan(String plan) { this.plan = plan; }
     public LocalDate getExpireDate() { return expireDate; }
     public void setExpireDate(LocalDate expireDate) { this.expireDate = expireDate; }
     public String getStatus() { return status; }
     public void setStatus(String status) { this.status = status; }
 }
