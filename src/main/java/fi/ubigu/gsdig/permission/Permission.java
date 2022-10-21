package fi.ubigu.gsdig.permission;

import java.util.Collection;
import java.util.UUID;

public class Permission {

    private final UUID resourceId;
    private final String role;
    private final Collection<PermissionType> permissions;

    public Permission(UUID resourceId, String role, Collection<PermissionType> permissions) {
        this.resourceId = resourceId;
        this.role = role;
        this.permissions = permissions;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getRole() {
        return role;
    }

    public Collection<PermissionType> getPermissions() {
        return permissions;
    }
    
}
