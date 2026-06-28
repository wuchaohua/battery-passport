 package com.battery.plugin.api;

 import com.battery.plugin.api.dto.BatteryData;
 import com.battery.plugin.api.dto.ValidateResult;

 /**
  * SPI 扩展点：电池数据校验器。
  * <p>
  * 客户方实现此接口，编写自定义校验逻辑。
  * 平台在导入/创建/更新电池护照时自动调用所有已注册的校验器。
  * </p>
  *
  * 使用示例：
  * <pre>
  * public class TrinaCapacityValidator implements BatteryDataValidator {
  *     &#64;Override
  *     public ValidateResult validate(BatteryData data) {
  *         // 天合储能自定义容量校验逻辑
  *     }
  * }
  * </pre>
  *
  * 注册方式：在 META-INF/services/com.battery.plugin.api.BatteryDataValidator
  * 中填写实现类的全限定名，将jar放入平台 services/plugins/ 目录即可热生效。
  */
 public interface BatteryDataValidator {
 
     /** 校验器唯一标识，用于日志和问题定位 */
     String validatorId();
 
     /** 执行校验 */
     ValidateResult validate(BatteryData data);
 
     /** 校验器优先级，数值越小越优先执行。默认 100 */
     default int order() { return 100; }
 
     /** 是否启用。平台可在运行时动态关闭某个校验器 */
     default boolean enabled() { return true; }
 }
