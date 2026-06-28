 package com.battery.mybatis;

 import com.baomidou.mybatisplus.annotation.DbType;
 import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
 import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
 import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
 import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
 import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
 import com.battery.common.TenantContext;
 import net.sf.jsqlparser.expression.Expression;
 import net.sf.jsqlparser.expression.LongValue;
 import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;

 import java.util.List;

 @Configuration
 public class MybatisPlusConfig {

     private static final List<String> IGNORE_TABLES = List.of(
             "sys_tenant", "sys_dict", "sys_config");

     /**
      * SaaS 模式: MyBatis-Plus 多租户拦截器
      */
     @Bean
     @ConditionalOnProperty(name = "battery.deploy.mode", havingValue = "saas")
     public MybatisPlusInterceptor saasInterceptor() {
         MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
         interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(
                 new TenantLineHandler() {
                     @Override
                     public Expression getTenantId() {
                         String tid = TenantContext.getTenantId();
                         return tid != null ? new LongValue(tid) : new LongValue(0);
                     }

                     @Override
                     public String getTenantIdColumn() { return "tenant_id"; }

                     @Override
                     public boolean ignoreTable(String tableName) {
                         return IGNORE_TABLES.contains(tableName);
                     }
                 }));
         interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
         return interceptor;
     }

     /**
      * 私有化模式: 仅分页，无租户拦截
      */
     @Bean
     @ConditionalOnProperty(name = "battery.deploy.mode", havingValue = "onpremise", matchIfMissing = true)
     public MybatisPlusInterceptor onpremiseInterceptor() {
         MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
         interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
         return interceptor;
     }
 }
