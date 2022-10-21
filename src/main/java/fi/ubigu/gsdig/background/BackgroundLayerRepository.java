package fi.ubigu.gsdig.background;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface BackgroundLayerRepository {

    public Collection<BackgroundLayer> findAll() throws Exception;
    public Optional<BackgroundLayer> findByUuid(UUID uuid) throws Exception;
    public BackgroundLayer create(BackgroundLayer backgroundLayer) throws Exception;
    public Optional<BackgroundLayer> update(UUID uuid, BackgroundLayer backgroundLayer) throws Exception;
    public void delete(UUID uuid) throws Exception;

}
