 package com.battery.plugin.api.dto;

 import java.util.Collections;
 import java.util.List;

 public class ValidateResult {

     private final boolean passed;
     private final List<String> errors;
     private final List<String> warnings;

     public static ValidateResult ok() {
         return new ValidateResult(true, Collections.emptyList(), Collections.emptyList());
     }

     public static ValidateResult fail(String... errorMessages) {
         return new ValidateResult(false, List.of(errorMessages), Collections.emptyList());
     }

     public static ValidateResult warn(String... warningMessages) {
         return new ValidateResult(true, Collections.emptyList(), List.of(warningMessages));
     }

     public ValidateResult(boolean passed, List<String> errors, List<String> warnings) {
         this.passed = passed;
         this.errors = errors != null ? errors : Collections.emptyList();
         this.warnings = warnings != null ? warnings : Collections.emptyList();
     }

     public boolean isPassed() { return passed; }
     public List<String> getErrors() { return errors; }
     public List<String> getWarnings() { return warnings; }
 }
