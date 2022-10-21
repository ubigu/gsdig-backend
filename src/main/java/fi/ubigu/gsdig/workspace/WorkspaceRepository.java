package fi.ubigu.gsdig.workspace;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {

    public Collection<Workspace> findAll(UUID userId) throws Exception;
    public Optional<Workspace> findByUuid(UUID uuid, UUID userId) throws Exception; 
    public Workspace create(Workspace workspace, UUID userId) throws Exception;
    public Optional<Workspace> update(UUID uuid, UUID userId, Workspace workspace) throws Exception;
    public void delete(UUID uuid, UUID userId) throws Exception;

}
