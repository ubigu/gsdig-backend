package fi.ubigu.gsdig.background;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.ubigu.gsdig.utility.JDBC;
import fi.ubigu.gsdig.utility.PGUtil;
import fi.ubigu.gsdig.utility.Utils;

@Component
public class BackgroundLayerRepositoryPG implements BackgroundLayerRepository {

    private static final String TABLE = "background_layer";
    private static final String SELECT = "SELECT uuid, type, title, options FROM " + TABLE;

    @Autowired
    private DataSource ds;

    @Autowired
    private ObjectMapper om;

    @Override
    public Collection<BackgroundLayer> findAll() throws Exception {
        return JDBC.findAll(ds, SELECT, this::parse);
    }

    @Override
    public Optional<BackgroundLayer> findByUuid(UUID uuid) throws Exception {
        return JDBC.findFirst(ds, SELECT + " WHERE uuid = ?",
                ps -> ps.setObject(1, uuid, Types.OTHER),
                this::parse);
    }

    private BackgroundLayer parse(ResultSet rs) throws Exception {
        int i = 1;

        UUID uuid = rs.getObject(i++, UUID.class);
        String type = rs.getString(i++);
        String title = rs.getString(i++);
        String optionsJson = rs.getString(i++);
        Map<String, Object> options = om.readValue(optionsJson, Utils.MAP_STRING_TO_OBJECT);

        return new BackgroundLayer(uuid, type, title, options);
    }

    @Override
    public BackgroundLayer create(BackgroundLayer backgroundLayer) throws Exception {
        UUID uuid = UUID.randomUUID();
        String insert = "INSERT INTO " + TABLE
                + " (uuid, type, title, options)"
                + " VALUES (?, ?, ?, ?)";
        JDBC.executeUpdate(ds, insert, ps -> {
            int i = 1;
            ps.setObject(i++, uuid, Types.OTHER);
            ps.setString(i++, backgroundLayer.getType());
            ps.setString(i++, backgroundLayer.getTitle());
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(backgroundLayer.getOptions())));
        });
        return findByUuid(uuid).orElseThrow();
    }

    @Override
    public Optional<BackgroundLayer> update(UUID uuid, BackgroundLayer backgroundLayer) throws Exception {
        String update = "UPDATE " + TABLE + " SET "
                + "type = ?,"
                + "title = ?,"
                + "options = ?"
                + " WHERE uuid = ?";
        JDBC.executeUpdate(ds, update, ps -> {
            int i = 1;
            ps.setString(i++, backgroundLayer.getType());
            ps.setString(i++, backgroundLayer.getTitle());
            ps.setObject(i++, PGUtil.toJsonObject(om.writeValueAsString(backgroundLayer.getOptions())));
            ps.setObject(i++, uuid, Types.OTHER);
        });
        return findByUuid(uuid);
    }

    @Override
    public void delete(UUID uuid) throws Exception {
        JDBC.executeUpdate(ds,
                "DELETE FROM " + TABLE + " WHERE uuid = ?",
                ps -> ps.setObject(1, uuid, Types.OTHER));
    }


}
