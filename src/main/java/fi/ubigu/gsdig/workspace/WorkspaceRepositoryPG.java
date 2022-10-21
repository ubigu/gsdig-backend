package fi.ubigu.gsdig.workspace;

import java.sql.ResultSet;
import java.sql.Types;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
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
public class WorkspaceRepositoryPG implements WorkspaceRepository {

    public static final TypeReference<List<LayerSettings>> TYPEREF_LIST_LAYERSETTINGS = new TypeReference<List<LayerSettings>>() {};

    private static final String TABLE = "workspace";
    private static final String SELECT = "SELECT "
            + "uuid, title, created_by, created_at, last_modified, center_x, center_y, zoom, background_layers, data_layer "
            + "FROM " + TABLE;

    @Autowired
    private DataSource ds;

    @Autowired
    private ObjectMapper om;

    @Override
    public Collection<Workspace> findAll(UUID userId) throws Exception {
        return JDBC.findAll(ds, SELECT + " WHERE created_by = ?",
                ps -> ps.setObject(1, userId, Types.OTHER),
                this::parse);
    }

    @Override
    public Optional<Workspace> findByUuid(UUID uuid, UUID userId) throws Exception {
        return JDBC.findFirst(ds, SELECT + " WHERE uuid = ? AND created_by = ?", ps -> {
            ps.setObject(1, uuid, Types.OTHER);
            ps.setObject(2, userId, Types.OTHER);
        }, this::parse);
    }

    private Workspace parse(ResultSet rs) throws Exception {
        int i = 1;

        UUID uuid = rs.getObject(i++, UUID.class);
        String title = rs.getString(i++);
        UUID createdBy = rs.getObject(i++, UUID.class);
        Instant createdAt = JDBC.fromSQLTimestamp(rs.getTimestamp(i++));
        Instant lastModifiedAt = JDBC.fromSQLTimestamp(rs.getTimestamp(i++));
        double centerX = rs.getDouble(i++);
        double centerY = rs.getDouble(i++);
        double zoom = rs.getDouble(i++);
        String backgroundLayersJson = rs.getString(i++);
        String dataLayersJson = rs.getString(i++);

        List<LayerSettings> backgroundLayers = om.readValue(backgroundLayersJson, TYPEREF_LIST_LAYERSETTINGS);
        LayerSettings dataLayer = dataLayersJson == null ? null : om.readValue(dataLayersJson, LayerSettings.class);

        Workspace ws = new Workspace();
        ws.setUuid(uuid);
        ws.setTitle(title);
        ws.setCreatedBy(createdBy);
        ws.setCreatedAt(createdAt);
        ws.setLastModifiedAt(lastModifiedAt);
        ws.setCenter(new double[] { centerX, centerY });
        ws.setZoom(zoom);
        ws.setBackgroundLayers(backgroundLayers);
        ws.setDataLayer(dataLayer);
        return ws;
    }

    @Override
    public Workspace create(Workspace workspace, UUID userId) throws Exception {
        UUID uuid = UUID.randomUUID();

        String insert = "INSERT INTO " + TABLE
                + " (uuid, title, created_by, center_x, center_y, zoom, background_layers, data_layer)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        StatementPreparer prepare = ps -> {
            int i = 1;
            ps.setObject(i++, uuid, Types.OTHER);
            ps.setString(i++, workspace.getTitle());
            ps.setObject(i++, userId, Types.OTHER);
            ps.setDouble(i++, workspace.getCenter()[0]);
            ps.setDouble(i++, workspace.getCenter()[1]);
            ps.setDouble(i++, workspace.getZoom());
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(workspace.getBackgroundLayers())));
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(workspace.getDataLayer())));
        };

        JDBC.executeUpdate(ds, insert, prepare);
        return findByUuid(uuid, userId).orElseThrow();
    }

    @Override
    public Optional<Workspace> update(UUID uuid, UUID userId, Workspace workspace) throws Exception {
        String update = "UPDATE " + TABLE + " SET "
                + "title = ?,"
                + "last_modified = now(),"
                + "center_x = ?,"
                + "center_y = ?,"
                + "zoom = ?,"
                + "background_layers = ?,"
                + "data_layer = ?"
                + " WHERE uuid = ? AND created_by = ?";
        StatementPreparer prep = ps -> {
            int i = 1;
            ps.setString(i++, workspace.getTitle());
            ps.setDouble(i++, workspace.getCenter()[0]);
            ps.setDouble(i++, workspace.getCenter()[1]);
            ps.setDouble(i++, workspace.getZoom());
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(workspace.getBackgroundLayers())));
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(workspace.getDataLayer())));
            ps.setObject(i++, uuid, Types.OTHER);
            ps.setObject(i++, userId, Types.OTHER);
        };

        JDBC.executeUpdate(ds, update, prep);
        return findByUuid(uuid, userId);
    }

    @Override
    public void delete(UUID uuid, UUID userId) throws Exception {
        JDBC.executeUpdate(ds, "DELETE FROM " + TABLE + " WHERE uuid = ? AND created_by = ?", ps -> {
            ps.setObject(1, uuid, Types.OTHER);
            ps.setObject(2, userId, Types.OTHER);
        });
    }

}
