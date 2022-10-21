package fi.ubigu.gsdig.download;

import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.ubigu.gsdig.utility.JDBC;
import fi.ubigu.gsdig.utility.StatementPreparer;

@Component
public class DownloadRepositoryPG implements DownloadRepository {

    private static final String TABLE = "downloads";

    @Autowired
    private DataSource ds;

    public Optional<UUID> getUUID(UUID collectionId, UUID userId, String format) throws Exception {
        String select = "SELECT uuid FROM " + TABLE + " WHERE collection_id = ? AND created_by = ? AND format = ?";
        StatementPreparer prep = ps -> {
            ps.setObject(1, collectionId, Types.OTHER);
            ps.setObject(2, userId, Types.OTHER);
            ps.setString(3, format);
        };
        return JDBC.findFirst(ds, select, prep, rs -> rs.getObject(1, UUID.class));
    }

    public void store(UUID uuid, UUID collectionId, UUID userId, String format, DownloadFile file) throws Exception {
        String insert = ""
                + "INSERT INTO " + TABLE + " (uuid, collection_id, created_by, format, path, content_type, filename, length)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        JDBC.executeUpdate(ds, insert, ps -> {
            int i = 1;
            ps.setObject(i++, uuid, Types.OTHER);
            ps.setObject(i++, collectionId, Types.OTHER);
            ps.setObject(i++, userId, Types.OTHER);
            ps.setString(i++, format);
            ps.setString(i++, file.getPath());
            ps.setString(i++, file.getContentType());
            ps.setString(i++, file.getFilename());
            ps.setLong(i++, file.getLength());
        });
    }

    @Override
    public Optional<DownloadFile> getByUuid(UUID uuid) throws Exception {
        String select = "SELECT path, content_type, filename, length FROM " + TABLE + " WHERE uuid = ?";
        return JDBC.findFirst(ds, select,
                ps -> ps.setObject(1, uuid, Types.OTHER),
                rs -> {
                    int i = 1;
                    String path = rs.getString(i++);
                    String contentType = rs.getString(i++);
                    String filename = rs.getString(i++);
                    long length = rs.getLong(i++);
                    return new DownloadFile(path, contentType, filename, length);
                }
        );
    }

    @Override
    public void delete(UUID uuid) throws Exception {
        String delete = "DELETE FROM " + TABLE + " WHERE uuid = ?";
        JDBC.executeUpdate(ds, delete, ps -> ps.setObject(1, uuid, Types.OTHER));
    }

}
