package fi.ubigu.gsdig.unitdata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.ubigu.gsdig.arealdivision.AttributeInfo;
import fi.ubigu.gsdig.utility.JDBC;
import fi.ubigu.gsdig.utility.PGUtil;
import fi.ubigu.gsdig.utility.StatementPreparer;

@Component
public class UnitDataRepositoryPG implements UnitDataRepository {

    private static final String TABLE_UNITDATA = "unitdata";
    private static final TypeReference<Map<String, AttributeInfo>> MAP_STRING_ATTRIBUTEINFO = new TypeReference<Map<String, AttributeInfo>>() {};

    private static final String SELECT = "SELECT uuid, created_by, title, description, organization, publicity, min_x, min_y, max_x, max_y, attributes, remote, sensitivity_setting FROM " + TABLE_UNITDATA;

    @Autowired
    private DataSource ds;

    @Autowired
    private ObjectMapper om;
    
    @Override
    public List<UnitDataset> findAll() throws Exception {
        return JDBC.findAll(ds, SELECT, this::parse);
    }

    @Override
    public List<UnitDataset> findAllPublic() throws Exception {
        String select = SELECT + " WHERE publicity = true";
        return JDBC.findAll(ds, select, this::parse);
    }

    @Override
    public Optional<UnitDataset> findByUuid(UUID uuid) throws Exception {
        try (Connection c = ds.getConnection()) {
            return findByUuid(c, uuid);
        }
    }

    private Optional<UnitDataset> findByUuid(Connection c, UUID uuid) throws Exception {
        String select = SELECT + " WHERE uuid = ?";
        return JDBC.findFirst(c, select, ps -> ps.setObject(1, uuid, Types.OTHER), this::parse);
    }

    @Override
    public Optional<UnitDataset> findPublicByUuid(UUID uuid) throws Exception {
        String select = SELECT + " WHERE uuid = ? AND publicity = true";
        return JDBC.findFirst(ds, select, ps -> ps.setObject(1, uuid, Types.OTHER), this::parse);
    }

    private UnitDataset parse(ResultSet rs) throws Exception {
        return parse(rs, null);
    }

    private UnitDataset parse(ResultSet rs, UUID userId) throws Exception {
        int i = 1;

        UUID uuid = rs.getObject(i++, UUID.class);
        UUID createdBy = rs.getObject(i++, UUID.class);
        String title = rs.getString(i++);
        String description = rs.getString(i++);
        String organization = rs.getString(i++);
        boolean publicity = rs.getBoolean(i++);
        double minx = rs.getDouble(i++);
        double miny = rs.getDouble(i++);
        double maxx = rs.getDouble(i++);
        double maxy = rs.getDouble(i++);
        double[] extent = { minx, miny, maxx, maxy };
        String strAttributes = rs.getString(i++);
        boolean remote = rs.getBoolean(i++);
        String strSensitivitySettings = rs.getString(i++);

        Map<String, AttributeInfo> attributes = null;
        if (strAttributes != null) {
            attributes = om.readValue(strAttributes, MAP_STRING_ATTRIBUTEINFO);
        }

        SensitivitySetting sensitivitySetting = null;
        if (strSensitivitySettings != null) {
            sensitivitySetting = om.readValue(strSensitivitySettings, SensitivitySetting.class);
        }

        return new UnitDataset(uuid, createdBy, title, description, organization, publicity, extent, attributes, remote, sensitivitySetting);
    }

    @Override
    public void create(UnitDataset metadata) throws Exception {
        String insert = "INSERT INTO " + TABLE_UNITDATA + " (uuid, created_by, title, description, organization, publicity, min_x, min_y, max_x, max_y, attributes, remote, sensitivity_setting)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        StatementPreparer prepare = ps -> {
            int i = 1;
            ps.setObject(i++, metadata.getUuid(), Types.OTHER);
            ps.setObject(i++, metadata.getCreatedBy(), Types.OTHER);
            ps.setString(i++, metadata.getTitle());
            ps.setString(i++, metadata.getDescription());
            ps.setString(i++, metadata.getOrganization());
            ps.setBoolean(i++, metadata.isPublicity());
            ps.setDouble(i++, metadata.getExtent()[0]);
            ps.setDouble(i++, metadata.getExtent()[1]);
            ps.setDouble(i++, metadata.getExtent()[2]);
            ps.setDouble(i++, metadata.getExtent()[3]);
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(metadata.getAttributes())));
            ps.setBoolean(i++, metadata.isRemote());
            if (metadata.getSensitivitySetting() == null) {
                ps.setNull(i++, Types.OTHER);
            } else {
                ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(metadata.getSensitivitySetting())));
            }
        };

        JDBC.executeUpdate(ds, insert, prepare);
    }

    @Override
    public boolean update(UnitDataset metadata) throws Exception {
        String update = "UPDATE " + TABLE_UNITDATA + " SET"
                + " title = ?,"
                + " description = ?, "
                + " organization = ?,"
                + " sensitivity_setting = ?,"
                + " publicity = ?,"
                + " attributes = ?"
                + " WHERE uuid = ?";
        StatementPreparer prepare = ps -> {
            int i = 1;
            ps.setString(i++, metadata.getTitle());
            ps.setString(i++, metadata.getDescription());
            ps.setString(i++, metadata.getOrganization());
            if (metadata.getSensitivitySetting() == null) {
                ps.setNull(i++, Types.OTHER);
            } else {
                ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(metadata.getSensitivitySetting())));
            }
            ps.setBoolean(i++, metadata.isPublicity());
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(metadata.getAttributes())));

            ps.setObject(i++, metadata.getUuid(), Types.OTHER);
        };

        return JDBC.executeUpdate(ds, update, prepare) == 1;
    }

    @Override
    public Optional<UnitDataset> delete(UUID uuid) throws Exception {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            Optional<UnitDataset> toDelete = findByUuid(c, uuid);
            if (toDelete.isEmpty()) {
                return toDelete;
            }
            
            String delete = String.format("DELETE FROM %s WHERE uuid = ?", TABLE_UNITDATA);
            int rowsChanged = JDBC.executeUpdate(ds, delete, ps -> ps.setObject(1, uuid, Types.OTHER));

            if (rowsChanged == 1) {
                c.commit();
                return toDelete;
            }
            
            return Optional.empty();
        }
    }

}
