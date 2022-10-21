package fi.ubigu.gsdig.download;

import java.util.Optional;
import java.util.UUID;

public interface DownloadRepository {

    public Optional<UUID> getUUID(UUID collectionId, UUID userId, String format) throws Exception;
    public void store(UUID uuid, UUID collectionId,  UUID userId, String format, DownloadFile file) throws Exception;
    public Optional<DownloadFile> getByUuid(UUID uuid) throws Exception;
    public void delete(UUID uuid) throws Exception;

}
