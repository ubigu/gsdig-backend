package fi.ubigu.gsdig.joins;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

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

@RestController
@RequestMapping("/joins")
public class JoinController {

    @Autowired
    private JoinService joinService;

    @PostMapping
    public JoinJob create(@RequestBody JoinRequest request, Principal principal) throws Exception {
        return joinService.create(request, principal);
    }

    @GetMapping("/{uuid}")
    public JoinJob findByUuid(@PathVariable UUID uuid) throws Exception {
        return joinService.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @GetMapping
    public List<JoinJob> findAcceptedByUnitDataset(@RequestParam UUID unitDataset) throws Exception {
        return joinService.findAcceptedByUnitDataset(unitDataset);
    }

    @PostMapping("/{uuid}/start")
    public JoinJob start(@PathVariable UUID uuid) throws Exception {
        return joinService.start(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @PostMapping("/{uuid}/finish")
    public JoinJob finish(@PathVariable UUID uuid) throws Exception {
        return joinService.finish(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @PostMapping("/{uuid}/fail")
    public JoinJob fail(@PathVariable UUID uuid, @RequestParam String message) throws Exception {
        return joinService.fail(uuid, message)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

}
