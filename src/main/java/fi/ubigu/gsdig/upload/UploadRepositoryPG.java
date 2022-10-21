package fi.ubigu.gsdig.upload;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.ubigu.gsdig.utility.JDBC;
import fi.ubigu.gsdig.utility.PGUtil;
import fi.ubigu.gsdig.utility.StatementPreparer;
import fi.ubigu.gsdig.utility.Utils;

@Component
public class UploadRepositoryPG implements UploadRepository {

    public static final String TABLE = "uploads";

    private static final String SELECT = String.format("SELECT uuid, typename, min_x, min_y, max_x, max_y, srid, attributes FROM %s", TABLE);

    @Autowired
    private DataSource ds;

    @Autowired
    private ObjectMapper om;

    @Override
    public List<UploadInfo> findAll(UUID userId) throws Exception {
        String select = SELECT + " WHERE created_by = ?";
        return JDBC.findAll(ds, select,
                ps -> ps.setObject(1, userId, Types.OTHER),
                this::parse
        );
    }

    @Override
    public Optional<UploadInfo> findByUuid(UUID uuid, UUID userId) throws Exception {
        String select = SELECT + " WHERE uuid = ? AND created_by = ?";
        StatementPreparer prep = ps -> {
            ps.setObject(1, uuid, Types.OTHER);
            ps.setObject(2, userId, Types.OTHER);
        };
        return JDBC.findFirst(ds, select, prep, this::parse);
    }

    private UploadInfo parse(ResultSet rs) throws Exception {
        int i = 1;
        UUID uuid = rs.getObject(i++, UUID.class);
        String typename = rs.getString(i++);
        double minx = rs.getDouble(i++);
        double miny = rs.getDouble(i++);
        double maxx = rs.getDouble(i++);
        double maxy = rs.getDouble(i++);
        double[] extent = { minx, miny, maxx, maxy };
        int srid = rs.getInt(i++);
        String attributesJson = rs.getString(i++);
        Map<String, Class<?>> attributes = om.readValue(attributesJson, Utils.MAP_STRING_TO_CLASS);

        return new UploadInfo(uuid, typename, extent, srid, attributes);
    }

    @Override
    public void create(UploadInfo info, UUID userId) throws Exception {
        UUID uuid = info.getUuid();
        String typeName = info.getTypeName();
        double[] extent = info.getExtent();
        int srid = info.getSrid();
        Map<String, Class<?>> attributes = info.getAttributes();
        
        String insert = String.format("INSERT INTO %s (uuid, created_by, typename, min_x, min_y, max_x, max_y, srid, attributes)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE);
        JDBC.executeUpdate(ds, insert, ps -> {
            int i = 1;
            ps.setObject(i++, uuid, Types.OTHER);
            ps.setObject(i++, userId, Types.OTHER);
            ps.setString(i++, typeName);
            ps.setDouble(i++, extent[0]);
            ps.setDouble(i++, extent[1]);
            ps.setDouble(i++, extent[2]);
            ps.setDouble(i++, extent[3]);
            ps.setInt(i++, srid);
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(attributes)));
        });
    }

    @Override
    public Optional<UploadInfo> delete(UUID uuid, UUID userId) throws Exception {
        Optional<UploadInfo> e = findByUuid(uuid, userId);
        if (e.isPresent()) {
            String delete = String.format("DELETE FROM %s WHERE uuid = ?", TABLE);
            JDBC.executeUpdate(ds, delete, ps -> ps.setObject(1, uuid, Types.OTHER));
        }
        return e;
    }

}
