package fi.ubigu.gsdig.workspace.defaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workspace-defaults")
public class WorkspaceDefaultsController {
    
    @Autowired
    private WorkspaceDefaultsRepository repository;

    @GetMapping
    public WorkspaceDefaults getConfig() throws Exception {
        return repository.getConfig();
    }

}
