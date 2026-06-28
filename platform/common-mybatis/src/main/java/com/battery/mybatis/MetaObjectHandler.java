 package com.battery.mybatis;

 import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
 import com.battery.common.TenantContext;
 import org.apache.ibatis.reflection.MetaObject;
 import org.springframework.stereotype.Component;

 import java.time.LocalDateTime;

 @Component
 public class BatteryMetaObjectHandler implements MetaObjectHandler {

     @Override
     public void insertFill(MetaObject metaObject) {
         this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
         this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
         this.strictInsertFill(metaObject, "tenantId", () -> {
             String tid = TenantContext.getTenantId();
             return tid != null ? tid : "0";
         }, String.class);
     }

     @Override
     public void updateFill(MetaObject metaObject) {
         this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
     }
 }
