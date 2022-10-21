package fi.ubigu.gsdig.workspace;

import java.security.Principal;
import java.util.Collection;
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

import fi.ubigu.gsdig.permission.User;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {
    
    @Autowired
    private WorkspaceRepository repository;
    
    @GetMapping
    public Collection<Workspace> findAll(Principal principal) throws Exception {
        return repository.findAll(User.getUserId(principal));
    }

    @GetMapping("/{uuid}")
    public Workspace findByUuid(@PathVariable UUID uuid, Principal principal) throws Exception {
        return repository.findByUuid(uuid, User.getUserId(principal))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @PostMapping
    public Workspace create(@RequestBody Workspace workspace, Principal principal) throws Exception {
        UUID userId = UUID.fromString(principal.getName());
        return repository.create(workspace, userId);
    }

    @PutMapping("/{uuid}")
    public Workspace update(@PathVariable UUID uuid, @RequestBody Workspace workspace, Principal principal) throws Exception {
        return repository.update(uuid, User.getUserId(principal), workspace)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID uuid, Principal principal) throws Exception {
        repository.delete(uuid, User.getUserId(principal));
    }
    
}
