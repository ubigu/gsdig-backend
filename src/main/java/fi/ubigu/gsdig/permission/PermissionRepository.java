package fi.ubigu.gsdig.permission;

import java.util.List;
import java.util.UUID;

public interface PermissionRepository {
    
    public List<Permission> findAllByResourceId(UUID resourceId) throws Exception;
    public List<Permission> findAllByRoles(List<String> roles) throws Exception;
    public void create(Permission permission) throws Exception;
    public void createIfNoneExists(Permission permission) throws Exception;
    public void delete(UUID resourceId, String role) throws Exception;

}
