package fi.ubigu.gsdig.background;

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

@RestController
@RequestMapping("/background-layers")
public class BackgroundLayerController {

    @Autowired
    private BackgroundLayerRepository repository;

    @GetMapping
    public Collection<BackgroundLayer> findAll() throws Exception {
        return repository.findAll();
    }

    @GetMapping("/{uuid}")
    public BackgroundLayer findByUuid(@PathVariable UUID uuid) throws Exception {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @PostMapping
    public BackgroundLayer create(@RequestBody BackgroundLayer backgroundLayer) throws Exception {
        return repository.create(backgroundLayer);
    }

    @PutMapping("/{uuid}")
    public BackgroundLayer update(@PathVariable UUID uuid, @RequestBody BackgroundLayer backgroundLayer) throws Exception {
        return repository.update(uuid, backgroundLayer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID uuid) throws Exception {
        repository.delete(uuid);
    }

}
