package fi.ubigu.gsdig.arealdivision;

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

import fi.ubigu.gsdig.utility.JDBC;
import fi.ubigu.gsdig.utility.PGUtil;
import fi.ubigu.gsdig.utility.StatementPreparer;

@Component
class ArealDivisionRepositoryPG implements ArealDivisionRepository {

    private static final String METADATA_TABLE = "areal_division";
    private static final TypeReference<Map<String, AttributeInfo>> MAP_STRING_ATTRIBUTEINFO = new TypeReference<Map<String, AttributeInfo>>() {};

    @Autowired
    private DataSource ds;

    @Autowired
    private ObjectMapper om;

    private static final String SELECT = "SELECT "
            + "uuid, created_by, title, description, organization, publicity, min_x, min_y, max_x, max_y, attributes"
            + " FROM " + METADATA_TABLE;

    @Override
    public List<ArealDivision> findAll() throws Exception {
        return JDBC.findAll(ds, SELECT, this::parse);
    }
    
    @Override
    public List<ArealDivision> findAllPublic() throws Exception {
        return JDBC.findAll(ds, SELECT + " WHERE publicity = true", this::parse);
    }

    @Override
    public Optional<ArealDivision> findByUuid(UUID uuid) throws Exception {
        try (Connection c = ds.getConnection()) {
            return findByUuid(c, uuid);
        }
    }
    
    private Optional<ArealDivision> findByUuid(Connection c, UUID uuid) throws Exception {
        String select = SELECT + " WHERE uuid = ?";
        return JDBC.findFirst(c, select, ps -> ps.setObject(1, uuid, Types.OTHER), this::parse);
    }

    @Override
    public Optional<ArealDivision> findPublicByUuid(UUID uuid) throws Exception {
        String select = SELECT + " WHERE publicity = true AND uuid = ?";
        return JDBC.findFirst(ds, select, ps -> ps.setObject(1, uuid, Types.OTHER), this::parse);
    }

    private ArealDivision parse(ResultSet rs) throws Exception {
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
        Map<String, AttributeInfo> attributes = om.readValue(rs.getString(i++), MAP_STRING_ATTRIBUTEINFO);

        return new ArealDivision(uuid, createdBy, title, description, organization, publicity, extent, attributes);
    }

    @Override
    public void create(ArealDivision arealDivision) throws Exception {
        String insert = "INSERT INTO " + METADATA_TABLE
                + " (uuid, created_by, title, description, organization, publicity, min_x, min_y, max_x, max_y, attributes)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        JDBC.executeUpdate(ds, insert, ps -> {
            int i = 1;
            ps.setObject(i++, arealDivision.getUuid(), Types.OTHER);
            ps.setObject(i++, arealDivision.getCreatedBy(), Types.OTHER);
            ps.setString(i++, arealDivision.getTitle());
            ps.setString(i++, arealDivision.getDescription());
            ps.setString(i++, arealDivision.getOrganization());
            ps.setBoolean(i++, arealDivision.isPublicity());
            ps.setDouble(i++, arealDivision.getExtent()[0]);
            ps.setDouble(i++, arealDivision.getExtent()[1]);
            ps.setDouble(i++, arealDivision.getExtent()[2]);
            ps.setDouble(i++, arealDivision.getExtent()[3]);
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(arealDivision.getAttributes())));
        });
    }

    @Override
    public Optional<ArealDivision> update(ArealDivision arealDivision) throws Exception {
        String update = "UPDATE " + METADATA_TABLE + " SET"
                + " title = ?,"
                + " description = ?, "
                + " organization = ?,"
                + " publicity = ?,"
                + " min_x = ?,"
                + " min_y = ?,"
                + " max_x = ?,"
                + " max_y = ?,"
                + " attributes = ?"
                + " WHERE uuid = ?";
        StatementPreparer prepare = ps -> {
            int i = 1;
            ps.setString(i++, arealDivision.getTitle());
            ps.setString(i++, arealDivision.getDescription());
            ps.setString(i++, arealDivision.getOrganization());
            ps.setBoolean(i++, arealDivision.isPublicity());
            ps.setDouble(i++, arealDivision.getExtent()[0]);
            ps.setDouble(i++, arealDivision.getExtent()[1]);
            ps.setDouble(i++, arealDivision.getExtent()[2]);
            ps.setDouble(i++, arealDivision.getExtent()[3]);
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(arealDivision.getAttributes())));

            ps.setObject(i++, arealDivision.getUuid(), Types.OTHER);
        };
        
        final Optional<ArealDivision> prev;
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            
            prev = findByUuid(c, arealDivision.uuid);
            if (prev.isEmpty() || JDBC.executeUpdate(c, update, prepare) != 1) {
                return Optional.empty();
            }

            c.commit();
        }
        return prev;
    }

    @Override
    public boolean delete(UUID uuid) throws Exception {
        String delete = String.format("DELETE FROM %s WHERE uuid = ?", METADATA_TABLE);
        return JDBC.executeUpdate(ds, delete, ps -> ps.setObject(1, uuid, Types.OTHER)) == 1;
    }

}
