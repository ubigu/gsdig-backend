package fi.ubigu.gsdig.unitdata;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.ubigu.gsdig.data.DataRepository;
import fi.ubigu.gsdig.permission.Permission;
import fi.ubigu.gsdig.permission.PermissionException;
import fi.ubigu.gsdig.permission.PermissionRepository;
import fi.ubigu.gsdig.permission.PermissionService;
import fi.ubigu.gsdig.permission.PermissionType;
import fi.ubigu.gsdig.permission.User;

@Component
public class UnitDataService {
    
    @Autowired
    private UnitDataRepository repo;
    
    @Autowired
    private DataRepository dataRepository;
    
    @Autowired
    private PermissionRepository permissionRepo;
    
    /**
     * @deprecated Internal usage only by DataAggregater
     */
    public List<UnitDataset> findAll() throws Exception {
        return repo.findAll();
    }
    
    public List<UnitDataset> findAll(Principal principal) throws Exception {
        List<String> roles = User.getRoles(principal);

        Map<UUID, List<Permission>> byId = permissionRepo.findAllByRoles(roles).stream()
                .collect(Collectors.groupingBy(Permission::getResourceId));
        
        return repo.findAll().stream()
                .filter(ud -> isReadable(ud, roles, byId.get(ud.getUuid())))
                .toList();
    }
    
    private boolean isReadable(UnitDataset ud, List<String> roles, List<Permission> permissions) {
        return ud.isPublicity() || PermissionService.hasPermission(roles, permissions, PermissionType.READ); 
    }

    public Optional<UnitDataset> findByUuid(UUID uuid, Principal principal) throws Exception {
        return repo.findByUuid(uuid);
    }

    public UnitDataset create(UnitDataset metadata) throws Exception {
        repo.create(metadata);
        permissionRepo.create(new Permission(metadata.getUuid(), metadata.getCreatedBy().toString(), PermissionType.ALL));
        return metadata;
    }

    public boolean update(UnitDataset metadata, Principal principal) throws Exception {
        checkPermission(principal, metadata.getUuid(), PermissionType.WRITE);
        return repo.update(metadata);
    }

    public void delete(UUID uuid, Principal principal) throws Exception {
        checkPermission(principal, uuid, PermissionType.WRITE);
        boolean doDrop = repo.delete(uuid)
                .map(it -> !it.isRemote())
                .orElse(false);
        if (doDrop) {
            dataRepository.drop(uuid);
        }
    }
    
    private void checkPermission(Principal principal, UUID resourceId, PermissionType type) throws Exception {
        List<String> roles = User.getRoles(principal);
        List<Permission> permissions = permissionRepo.findAllByResourceId(resourceId);
        if (!PermissionService.hasPermission(roles, permissions, type)) {
            throw new PermissionException();
        }
    }

}
