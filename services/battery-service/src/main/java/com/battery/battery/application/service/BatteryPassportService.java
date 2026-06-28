 package com.battery.battery.application.service;

 import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
 import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
 import com.battery.battery.domain.model.BatteryPassport;
 import com.battery.battery.infrastructure.repository.BatteryPassportMapper;
 import com.battery.battery.infrastructure.config.PluginManager;
 import com.battery.common.BatteryException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;

 import java.util.List;
 import java.util.UUID;

 @Service
 public class BatteryPassportService {

     private final BatteryPassportMapper mapper;
     private final PluginManager pluginManager;

     public BatteryPassportService(BatteryPassportMapper mapper, PluginManager pluginManager) {
         this.mapper = mapper;
         this.pluginManager = pluginManager;
     }

     public Page<BatteryPassport> list(int page, int size, String status, String manufacturer) {
         LambdaQueryWrapper<BatteryPassport> qw = new LambdaQueryWrapper<>();
         if (status != null) qw.eq(BatteryPassport::getStatus, status);
         if (manufacturer != null) qw.eq(BatteryPassport::getManufacturer, manufacturer);
         qw.orderByDesc(BatteryPassport::getCreateTime);
         return mapper.selectPage(new Page<>(page, size), qw);
     }

     public BatteryPassport getByPassportId(String passportId) {
         BatteryPassport p = mapper.findByPassportId(passportId);
         if (p == null) throw new BatteryException(404, "电池护照不存在: " + passportId);
         return p;
     }

     @Transactional
     public String create(BatteryPassport passport) {
         // 执行客户自定义校验插件
         pluginManager.validate(passport);

         passport.setPassportId("BP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
         passport.setStatus("ACTIVE");
         mapper.insert(passport);
         return passport.getPassportId();
     }

     @Transactional
     public void update(String passportId, BatteryPassport passport) {
         BatteryPassport existing = getByPassportId(passportId);
         passport.setId(existing.getId());
         passport.setPassportId(passportId);
         mapper.updateById(passport);
     }

     @Transactional
     public void delete(String passportId) {
         mapper.deleteByMap(java.util.Map.of("passport_id", passportId));
     }

     public List<BatteryPassport> searchBySerialNumber(String sn) {
         return mapper.findBySerialNumber(sn);
     }
 }
