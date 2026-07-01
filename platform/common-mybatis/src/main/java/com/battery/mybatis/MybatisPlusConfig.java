package com.battery.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.battery.common.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@Configuration
public class MybatisPlusConfig {

    private static final Logger log = LoggerFactory.getLogger(MybatisPlusConfig.class);
    private static final List<String> IGNORE_TABLES = List.of(
            "sys_tenant", "sys_dict", "sys_config");

    private final DataSource dataSource;

    public MybatisPlusConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Auto-detect the database type from the configured DataSource JDBC URL.
     * Supports: MySQL 8.x, PostgreSQL 18 (and other standard DbType values).
     */
    private DbType detectDbType() {
        try (Connection conn = dataSource.getConnection()) {
            String jdbcUrl = conn.getMetaData().getURL();
            DbType dbType = JdbcUtils.getDbType(jdbcUrl);
            log.info("Detected database type: {} (JDBC URL: {})", dbType, jdbcUrl);
            return dbType != null ? dbType : DbType.MYSQL;
        } catch (Exception e) {
            log.warn("Failed to detect database type, defaulting to MYSQL: {}", e.getMessage());
            return DbType.MYSQL;
        }
    }

    /**
     * SaaS mode: MyBatis-Plus multi-tenant interceptor with auto-detected DbType.
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
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(detectDbType()));
        return interceptor;
    }

    /**
     * On-premise mode: pagination only, no tenant interceptor. Auto-detected DbType.
     */
    @Bean
    @ConditionalOnProperty(name = "battery.deploy.mode", havingValue = "onpremise", matchIfMissing = true)
    public MybatisPlusInterceptor onpremiseInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(detectDbType()));
        return interceptor;
    }
}