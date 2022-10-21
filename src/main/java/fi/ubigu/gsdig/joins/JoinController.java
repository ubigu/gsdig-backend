package fi.ubigu.gsdig.joins;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fi.ubigu.gsdig.arealdivision.ArealDivision;
import fi.ubigu.gsdig.arealdivision.ArealDivisionService;
import fi.ubigu.gsdig.permission.Permission;
import fi.ubigu.gsdig.permission.PermissionRepository;
import fi.ubigu.gsdig.permission.PermissionType;
import fi.ubigu.gsdig.permission.User;
import fi.ubigu.gsdig.unitdata.UnitDataService;
import fi.ubigu.gsdig.unitdata.UnitDataset;

@RestController
@RequestMapping("/joins")
public class JoinController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ArealDivisionService arealDivisionService;

    @Autowired
    private UnitDataService unitDataService;

    @Autowired
    private PermissionRepository permissionRepository;

    @PostMapping
    public JoinJob create(
            @RequestBody JoinRequest request,
            Principal principal) throws Exception {

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

    @GetMapping("/{uuid}")
    public JoinJob findByUuid(@PathVariable UUID uuid) throws Exception {
        return jobRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @GetMapping
    public List<JoinJob> findAcceptedByUnitDataset(@RequestParam UUID unitDataset) throws Exception {
        return jobRepository.findAcceptedJobsByUnitDataset(unitDataset);
    }

    @PostMapping("/{uuid}/start")
    public JoinJob start(@PathVariable UUID uuid) throws Exception {
        return jobRepository.start(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @PostMapping("/{uuid}/finish")
    public JoinJob finish(@PathVariable UUID uuid) throws Exception {
        return jobRepository.finish(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @PostMapping("/{uuid}/fail")
    public JoinJob fail(@PathVariable UUID uuid, @RequestParam String message) throws Exception {
        return jobRepository.error(uuid, message)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

}
