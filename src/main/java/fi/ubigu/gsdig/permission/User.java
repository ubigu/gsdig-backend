package fi.ubigu.gsdig.permission;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class User {
    
    public static UUID getUserId(Principal principal) {
        UUID userId = UUID.fromString(getUserIdAsString(principal));
        return userId;
    }
    
    public static String getUserIdAsString(Principal principal) {
        return principal.getName();
    }
    
    public static List<String> getRoles(Principal principal) {
        if (principal == null) {
            return Collections.emptyList();
        }
        // Everyone get's their own uuid as a role + ones from keycloak
        List<String> roles = new ArrayList<>();
        roles.add(getUserIdAsString(principal));
        if (principal instanceof Authentication) {
            Authentication auth = (Authentication) principal;
            auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .forEach(roles::add);
        }
        return roles;
    }

}
