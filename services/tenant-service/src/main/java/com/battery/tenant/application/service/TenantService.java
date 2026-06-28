 package com.battery.tenant.application.service;

 import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
 import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
 import com.battery.tenant.domain.model.Tenant;
 import com.battery.tenant.infrastructure.repository.TenantMapper;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;

 @Service
 public class TenantService {

     private final TenantMapper mapper;

     public TenantService(TenantMapper mapper) { this.mapper = mapper; }

     public Page<Tenant> list(int page, int size) {
         return mapper.selectPage(new Page<>(page, size),
                 new LambdaQueryWrapper<Tenant>().orderByDesc(Tenant::getCreateTime));
     }

     public Tenant getById(Long id) { return mapper.selectById(id); }
     public Tenant getByCode(String code) { return mapper.selectOne(new LambdaQueryWrapper<Tenant>().eq(Tenant::getTenantCode, code)); }

     @Transactional
     public void create(Tenant tenant) {
         tenant.setStatus("ACTIVE");
         mapper.insert(tenant);
     }

     @Transactional
     public void update(Long id, Tenant tenant) {
         tenant.setId(id);
         mapper.updateById(tenant);
     }

     @Transactional
     public void delete(Long id) { mapper.deleteById(id); }
 }
