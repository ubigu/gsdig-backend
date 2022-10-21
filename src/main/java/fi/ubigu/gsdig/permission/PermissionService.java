package fi.ubigu.gsdig.permission;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class PermissionService {

    private PermissionRepository repo;

    public boolean hasPermission(List<String> roles, UUID resourceId, PermissionType type) throws Exception {
        List<Permission> permissions = repo.findAllByResourceId(resourceId);
        return hasPermission(roles, permissions, type);
    }

    public static boolean hasPermission(List<String> roles, List<Permission> permissions, PermissionType type) {
        if (roles == null || permissions == null || roles.isEmpty() || permissions.isEmpty()) {
            return false;
        }
        for (String role : roles) {
            Permission permission = permissions.stream()
                    .filter(it -> it.getRole().equals(role))
                    .findAny()
                    .orElse(null);
            if (permission != null && permission.getPermissions().contains(type)) {
                return true;
            }
        }
        return false;
    }    
}
