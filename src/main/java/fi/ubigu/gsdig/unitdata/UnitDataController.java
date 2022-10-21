package fi.ubigu.gsdig.unitdata;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fi.ubigu.gsdig.DatasetMetadata;
import fi.ubigu.gsdig.arealdivision.AttributeInfo;
import fi.ubigu.gsdig.permission.User;

@RestController
@RequestMapping("/unitdata")
public class UnitDataController {

    @Autowired
    private UnitDataService service;
    
    @GetMapping
    public List<UnitDataset> findAll(Principal principal) throws Exception {
        return service.findAll(principal);
    }

    @GetMapping("/{uuid}")
    public UnitDataset findByUuid(@PathVariable UUID uuid, Principal principal) throws Exception {
        return service.findByUuid(uuid, principal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @PostMapping
    public UnitDataset createRemote(@RequestBody DatasetMetadata meta, Principal principal) throws Exception {
        // This endpoint can only be used for creating remote UnitDatasets
        UUID uuid = UUID.randomUUID();
        UUID createdBy = User.getUserId(principal);
        String title = meta.getTitle();
        String description = meta.getDescription();
        String organization = meta.getOrganization();
        boolean publicity = meta.isPublicity();
        double[] extent = meta.getExtent();
        Map<String, AttributeInfo> attributes = meta.getAttributes();
        boolean remote = true; 
        SensitivitySetting ss = null;
        UnitDataset metadata = new UnitDataset(uuid, createdBy, title, description, organization, publicity, extent, attributes, remote, ss);  
        return service.create(metadata);
    }

    @PutMapping("/{uuid}")
    public UnitDataset update(
            @PathVariable UUID uuid,
            @RequestBody UnitDataset metadata,
            Principal principal) throws Exception {
        metadata = metadata.withUuid(uuid);
        if (!service.update(metadata, principal)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
        }
        return metadata;
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID uuid, Principal principal) throws Exception {
        service.delete(uuid, principal);
    }

}
