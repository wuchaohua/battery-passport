package com.battery.auth.infrastructure.adapter;

import com.battery.common.BatteryException;
import com.battery.security.SsoAdapter;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TrinaCasSsoAdapter implements SsoAdapter {

    @Value("${battery.sso.trina.cas-server-url:https://cas.trinasolar.com}")
    private String casServerUrl;

    @Value("${battery.sso.trina.cas-service-url:https://battery.trinasolar.com/api/v1/auth/callback}")
    private String casServiceUrl;

    @Override
    public String protocol() {
        return "CAS";
    }

    @Override
    public String buildLoginUrl(String redirectUri) {
        String service = (redirectUri != null && !redirectUri.isEmpty()) ? redirectUri : casServiceUrl;
        return casServerUrl + "/login?service=" + encodeUrl(service);
    }

    @Override
    public PlatformUser handleCallback(String ticket, String state) {
        if (ticket == null || ticket.isEmpty()) {
            throw new BatteryException("CAS ticket is required");
        }

        TicketValidator validator = new Cas30ServiceTicketValidator(casServerUrl);
        try {
            Assertion assertion = validator.validate(ticket, casServiceUrl);
            return extractUserFromAssertion(assertion);
        } catch (TicketValidationException e) {
            throw new BatteryException("CAS ticket validation failed: " + e.getMessage());
        }
    }

    @Override
    public PlatformUser validateToken(String token) {
        return null;
    }

    private PlatformUser extractUserFromAssertion(Assertion assertion) {
        AttributePrincipal principal = assertion.getPrincipal();
        String userId = principal.getName();

        PlatformUser user = new PlatformUser();
        user.setUserId(userId);
        user.setUsername(userId);
        user.setEnterpriseCode("trina");

        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> principalAttributes = principal.getAttributes();
        if (principalAttributes != null && !principalAttributes.isEmpty()) {
            attributes.putAll(principalAttributes);

            if (principalAttributes.containsKey("displayName")) {
                user.setDisplayName(String.valueOf(principalAttributes.get("displayName")));
            } else if (principalAttributes.containsKey("cn")) {
                user.setDisplayName(String.valueOf(principalAttributes.get("cn")));
            } else {
                user.setDisplayName(userId);
            }

            if (principalAttributes.containsKey("email")) {
                Object emailAttr = principalAttributes.get("email");
                if (emailAttr instanceof String) {
                    user.setEmail((String) emailAttr);
                } else if (emailAttr instanceof Object[]) {
                    Object[] emails = (Object[]) emailAttr;
                    if (emails.length > 0) {
                        user.setEmail(String.valueOf(emails[0]));
                    }
                }
            }

            if (principalAttributes.containsKey("department")) {
                attributes.put("department", principalAttributes.get("department"));
            }
            if (principalAttributes.containsKey("employeeNumber")) {
                attributes.put("employeeId", principalAttributes.get("employeeNumber"));
            }
        }

        user.setAttributes(attributes);
        return user;
    }

    private String encodeUrl(String url) {
        try {
            return java.net.URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            return url;
        }
    }
}