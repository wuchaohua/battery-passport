 package com.battery.battery.infrastructure.repository;

 import com.baomidou.mybatisplus.core.mapper.BaseMapper;
 import com.battery.battery.domain.model.BatteryPassport;
 import org.apache.ibatis.annotations.Mapper;
 import org.apache.ibatis.annotations.Param;
 import org.apache.ibatis.annotations.Select;

 import java.util.List;

 @Mapper
 public interface BatteryPassportMapper extends BaseMapper<BatteryPassport> {

     @Select("SELECT * FROM battery_passport WHERE serial_number = #{sn} AND deleted = 0")
     List<BatteryPassport> findBySerialNumber(@Param("sn") String serialNumber);

     @Select("SELECT * FROM battery_passport WHERE passport_id = #{pid} AND deleted = 0")
     BatteryPassport findByPassportId(@Param("pid") String passportId);
 }
