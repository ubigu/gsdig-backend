package fi.ubigu.gsdig.joins;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.ubigu.gsdig.arealdivision.ArealDivision;
import fi.ubigu.gsdig.arealdivision.ArealDivisionService;
import fi.ubigu.gsdig.permission.Permission;
import fi.ubigu.gsdig.permission.PermissionRepository;
import fi.ubigu.gsdig.permission.PermissionType;
import fi.ubigu.gsdig.permission.User;
import fi.ubigu.gsdig.unitdata.UnitDataService;
import fi.ubigu.gsdig.unitdata.UnitDataset;

@Component
public class JoinService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ArealDivisionService arealDivisionService;

    @Autowired
    private UnitDataService unitDataService;

    @Autowired
    private PermissionRepository permissionRepository;

    public JoinJob create(JoinRequest request, Principal principal) throws Exception {
        UUID arealDivisionId = request.getArealDivision();
        ArealDivision arealDivision = arealDivisionService.findById(arealDivisionId, principal)
                .orElseThrow(() -> new IllegalArgumentException("Unknown areal division"));

        UUID unitDatasetId = request.getUnitDataset();
        UnitDataset unitDataset = unitDataService.findByUuid(unitDatasetId, principal)
                .orElseThrow(() -> new IllegalArgumentException("Unknown unit dataset"));

        List<JoinAttribute> dataAttributes = request.getDataAttributes();
        dataAttributes = dataAttributes.stream()
                .filter(it -> it.getAggregate() != null && !it.getAggregate().isEmpty())
                .collect(Collectors.toList());
        request.setDataAttributes(dataAttributes);

        // TODO: validate attributes (make sure each exists in unit data)

        if (!arealDivision.isPublicity() && unitDataset.isRemote()) {
            addReadPermission(arealDivision, unitDataset);
        }

        UUID userId = User.getUserId(principal);
        return jobRepository.create(request, userId);
    }

    private void addReadPermission(ArealDivision ad, UnitDataset ud) throws Exception {
        // Make sure remote microservice has permission to read the areal division
        UUID remoteUser = ud.getCreatedBy();
        Permission permission = new Permission(
                ad.getUuid(),
                remoteUser.toString(),
                Arrays.asList(PermissionType.READ));
        permissionRepository.createIfNoneExists(permission);
    }

    public Optional<JoinJob> findByUuid(UUID uuid) throws Exception {
        return jobRepository.findByUuid(uuid);
    }

    public List<JoinJob> findAcceptedByUnitDataset(UUID unitDataset) throws Exception {
        return jobRepository.findAcceptedJobsByUnitDataset(unitDataset);
    }

    public Optional<JoinJob> start(UUID uuid) throws Exception {
        return jobRepository.start(uuid);
    }

    public Optional<JoinJob> finish(UUID uuid) throws Exception {
        return jobRepository.finish(uuid);
    }

    public Optional<JoinJob> fail(UUID uuid, String message) throws Exception {
        return jobRepository.error(uuid, message);
    }

}
