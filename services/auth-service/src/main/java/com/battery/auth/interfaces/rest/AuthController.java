 package com.battery.auth.interfaces.rest;

 import com.battery.auth.application.service.AuthService;
 import com.battery.common.Result;
 import jakarta.servlet.http.HttpServletResponse;
 import org.springframework.web.bind.annotation.*;

 import java.io.IOException;
 import java.util.Map;

 @RestController
 @RequestMapping("/api/v1/auth")
 public class AuthController {

     private final AuthService authService;

     public AuthController(AuthService authService) { this.authService = authService; }

     @GetMapping("/login")
     public Result<Map<String, String>> login(@RequestParam(required = false) String redirectUri) {
         String url = authService.getLoginUrl(redirectUri);
         return Result.ok(Map.of("loginUrl", url));
     }

     @GetMapping("/callback")
     public void callback(@RequestParam String code,
                          @RequestParam(required = false) String state,
                          HttpServletResponse response) throws IOException {
         String token = authService.handleCallback(code, state);
         response.sendRedirect("/?token=" + token);
     }

     @PostMapping("/token/validate")
     public Result<Map<String, Object>> validateToken(@RequestBody Map<String, String> body) {
         return Result.ok(authService.validateToken(body.get("token")));
     }

     @PostMapping("/logout")
     public Result<Void> logout(@RequestHeader("Authorization") String token) {
         authService.logout(token);
         return Result.ok();
     }
 }
