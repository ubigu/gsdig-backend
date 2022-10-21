package fi.ubigu.gsdig.arealdivision;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
public class ArealDivisionService {

    @Autowired
    private ArealDivisionRepository repo;
    
    @Autowired
    private DataRepository dataRepository;
    
    @Autowired
    private PermissionRepository permissionRepo;

    public List<ArealDivision> findAll(Principal principal) throws Exception {
        List<String> roles = User.getRoles(principal);

        Map<UUID, List<Permission>> byId = permissionRepo.findAllByRoles(roles).stream()
                .collect(Collectors.groupingBy(Permission::getResourceId));
        
        return repo.findAll().stream()
                .filter(ad -> isReadable(ad, roles, byId.get(ad.getUuid())))
                .toList();
    }
    
    private boolean isReadable(ArealDivision ad, List<String> roles, List<Permission> permissions) {
        return ad.isPublicity() || PermissionService.hasPermission(roles, permissions, PermissionType.READ); 
    }
    
    public List<ArealDivision> findAllPublic() throws Exception {
        return repo.findAllPublic();
    }

    public Optional<ArealDivision> findById(UUID uuid, Principal principal) throws Exception {
        if (principal == null) {
            return findPublicById(uuid);
        }
        return findById(uuid, principal, PermissionType.READ);
    }

    /**
     * @deprecated Internal usage only by DataAggregater
     */
    public Optional<ArealDivision> findById(UUID uuid) throws Exception {
        return repo.findByUuid(uuid);
    }
    
    public Optional<ArealDivision> findById(UUID uuid, Principal principal, PermissionType type) throws Exception {
        Optional<ArealDivision> opt = repo.findByUuid(uuid);
        if (opt.isPresent()) {
            boolean shouldCheck = type != PermissionType.READ || !opt.get().isPublicity();
            if (shouldCheck) {
                checkPermission(principal, uuid, type);
            }
        }
        return opt;
    }

    public Optional<ArealDivision> findPublicById(UUID uuid) throws Exception {
        return repo.findPublicByUuid(uuid);
    }

    public void create(ArealDivision ad) throws Exception {
        repo.create(ad);
        permissionRepo.create(new Permission(ad.getUuid(), ad.getCreatedBy().toString(), PermissionType.ALL));
    }

    public Optional<ArealDivision> clone(UUID uuid, Principal principal) throws Exception {
        Optional<ArealDivision> opt = findById(uuid, principal);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        ArealDivision toClone = opt.get();

        UUID userId = User.getUserId(principal);

        ArealDivision cloned = toClone
                .withUuid(UUID.randomUUID())
                .withCreatedBy(userId)
                .withPublicity(false)
                .withTitle(toClone.getTitle() + " clone");

        repo.create(cloned);
        dataRepository.clone(uuid, cloned.getUuid());
        permissionRepo.create(new Permission(cloned.getUuid(), userId.toString(), PermissionType.ALL));

        return Optional.of(cloned);
    }

    public Optional<ArealDivision> update(ArealDivision ad, Principal principal) throws Exception {
        checkPermission(principal, ad.getUuid(), PermissionType.WRITE);

        Optional<ArealDivision> prevOpt = repo.update(ad);
        if (prevOpt.isEmpty()) {
            return Optional.empty();
        }
        updateAttributes(prevOpt.get(), ad);

        return prevOpt;
    }

    private void updateAttributes(ArealDivision prev, ArealDivision ad) throws Exception {
        Set<String> newAttributeNames = ad.getAttributes().keySet();
        List<String> removedAttributes = prev.getAttributes().keySet().stream()
                .filter(k -> !newAttributeNames.contains(k))
                .toList();
        for (String attribute : removedAttributes) {
            dataRepository.removeAttribute(ad.getUuid(), attribute);
        }
    }

    public void delete(UUID uuid, Principal principal) throws Exception {
        checkPermission(principal, uuid, PermissionType.WRITE);

        repo.delete(uuid);
        dataRepository.drop(uuid);
    }
    
    private void checkPermission(Principal principal, UUID resourceId, PermissionType type) throws Exception {
        List<String> roles = User.getRoles(principal);
        List<Permission> permissions = permissionRepo.findAllByResourceId(resourceId);
        if (!PermissionService.hasPermission(roles, permissions, type)) {
            throw new PermissionException();
        }
    }

    public Map<String, String> validate(UUID uuid, Principal principal) throws Exception {
        checkPermission(principal, uuid, PermissionType.READ);
        return dataRepository.validate(uuid);
    }
    
}
