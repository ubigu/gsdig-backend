package fi.ubigu.gsdig.joins;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository {

    public JoinJob create(JoinRequest request, UUID userId) throws Exception;
    public List<JoinJob> findAcceptedJobsByUnitDataset(UUID unitDataset) throws Exception;
    public Optional<JoinJob> findByUuid(UUID jobId) throws Exception;
    public Optional<JoinJob> start(UUID jobId) throws IllegalStateException, Exception;
    public Optional<JoinJob> finish(UUID jobId) throws IllegalStateException, Exception;
    public Optional<JoinJob> error(UUID jobId, String message) throws IllegalStateException, Exception;

}
