package fi.ubigu.gsdig.arealdivision;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/arealdivisions")
public class ArealDivisionController {

    @Autowired
    private ArealDivisionService service;

    @GetMapping
    @ResponseBody
    public List<ArealDivision> findAll(Principal principal) throws Exception {
        return service.findAll(principal);
    }
    
    @GetMapping("/{uuid}")
    public ArealDivision findById(
            @PathVariable UUID uuid,
            Principal principal) throws Exception {
        return service.findById(uuid, principal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @PostMapping("/{uuid}/validate")
    public Map<String, String> validate(@PathVariable UUID uuid, Principal principal) throws Exception {
        return service.validate(uuid, principal);
    }

    @PostMapping("/{uuid}/clone")
    public ArealDivision clone(
            @PathVariable UUID uuid,
            Principal principal) throws Exception {
        return service.clone(uuid, principal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @PutMapping("/{uuid}")
    public ArealDivision update(
            @PathVariable UUID uuid,
            @RequestBody ArealDivision ad,
            Principal principal) throws Exception {
        ad = ad.withUuid(uuid);
        service.update(ad, principal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
        return ad;
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID uuid,
            Principal principal) throws Exception {
        service.delete(uuid, principal);
    }
    
}
