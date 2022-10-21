package fi.ubigu.gsdig.upload;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadRepository {

    public List<UploadInfo> findAll(UUID userId) throws Exception;
    public Optional<UploadInfo> findByUuid(UUID uuid, UUID userId) throws Exception;
    public Optional<UploadInfo> delete(UUID uuid, UUID userId) throws Exception;
    public void create(UploadInfo info, UUID userId) throws Exception;

}
