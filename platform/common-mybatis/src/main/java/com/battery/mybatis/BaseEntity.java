 package com.battery.mybatis;

 import com.baomidou.mybatisplus.annotation.*;
 import java.time.LocalDateTime;

 public class BaseEntity {

     @TableId(type = IdType.ASSIGN_ID)
     private Long id;

     @TableField(fill = FieldFill.INSERT)
     private String tenantId;

     @TableField(fill = FieldFill.INSERT)
     private LocalDateTime createTime;

     @TableField(fill = FieldFill.INSERT_UPDATE)
     private LocalDateTime updateTime;

     @TableLogic
     private Integer deleted;

     public Long getId() { return id; }
     public void setId(Long id) { this.id = id; }
     public String getTenantId() { return tenantId; }
     public void setTenantId(String tenantId) { this.tenantId = tenantId; }
     public LocalDateTime getCreateTime() { return createTime; }
     public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
     public LocalDateTime getUpdateTime() { return updateTime; }
     public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
     public Integer getDeleted() { return deleted; }
     public void setDeleted(Integer deleted) { this.deleted = deleted; }
 }
