package fi.ubigu.gsdig.workspace.defaults;

import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.ubigu.gsdig.utility.JDBC;
import fi.ubigu.gsdig.workspace.LayerSettings;

@Component
public class WorkspaceDefaultsRepositoryPostGIS implements WorkspaceDefaultsRepository {

    @Autowired
    private DataSource ds;

    @Override
    public WorkspaceDefaults getConfig() throws Exception {
        String select = "SELECT "
                + "title,"
                + "center_x,"
                + "center_y,"
                + "zoom,"
                + "background_layer,"
                + "opacity "
                + "FROM workspace_defaults";
        return JDBC.findFirst(ds, select, rs -> {
            int i = 1;

            String title = rs.getString(i++);

            double x = rs.getDouble(i++);
            double y = rs.getDouble(i++);
            double[] center = { x, y };

            double zoom = rs.getDouble(i++);

            UUID uuid = rs.getObject(i++, UUID.class);
            double opacity = rs.getDouble(i++);

            LayerSettings bg = uuid == null ? null
                    : new LayerSettings(uuid, opacity, true);

            return new WorkspaceDefaults(title, center, zoom, bg);
        }).orElseGet(this::getFallback);
    }

    private WorkspaceDefaults getFallback() {
        return null;
    }

}
