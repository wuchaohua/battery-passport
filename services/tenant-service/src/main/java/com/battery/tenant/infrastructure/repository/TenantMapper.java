 package com.battery.tenant.infrastructure.repository;

 import com.baomidou.mybatisplus.core.mapper.BaseMapper;
 import com.battery.tenant.domain.model.Tenant;
 import org.apache.ibatis.annotations.Mapper;

 @Mapper
 public interface TenantMapper extends BaseMapper<Tenant> {}
