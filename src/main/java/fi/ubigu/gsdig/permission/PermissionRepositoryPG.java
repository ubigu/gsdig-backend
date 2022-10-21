package fi.ubigu.gsdig.permission;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.ubigu.gsdig.utility.JDBC;

@Component
public class PermissionRepositoryPG implements PermissionRepository {
    
    private static final String TABLE = "permission";
    private static final String SELECT = "SELECT resource_id, role, permissions FROM " + TABLE;
    
    @Autowired
    private DataSource ds;

    @Override
    public List<Permission> findAllByResourceId(UUID resourceId) throws Exception {
        return JDBC.findAll(ds,
                SELECT + " WHERE resource_id = ?", 
                ps -> ps.setObject(1, resourceId, Types.OTHER), 
                this::parse
        );
    }

    @Override
    public List<Permission> findAllByRoles(List<String> roles) throws Exception {
        try (Connection c = ds.getConnection()) {
            Array roleArray = c.createArrayOf("varchar", roles.toArray());
            return JDBC.findAll(c,
                    SELECT + " WHERE role = any(?)", 
                    ps -> ps.setArray(1, roleArray),
                    this::parse);
        }
    }
    
    private Permission parse(ResultSet rs) throws Exception {
        int i = 1;

        UUID resouceId = rs.getObject(i++, UUID.class);
        String role = rs.getString(i++);
        int permissions = rs.getInt(i++);

        return new Permission(resouceId, role, unpack(permissions));
    }

    private static final String INSERT = "INSERT INTO " + TABLE + " (resource_id, role, permissions) VALUES (?, ?, ?)";

    @Override
    public void create(Permission permission) throws Exception {
        JDBC.executeUpdate(ds, INSERT, ps -> {
            int i = 1;
            ps.setObject(i++, permission.getResourceId(), Types.OTHER);
            ps.setString(i++, permission.getRole());
            ps.setInt(i++, pack(permission.getPermissions()));
        });
    }

    @Override
    public void createIfNoneExists(Permission permission) throws Exception {
        JDBC.executeUpdate(ds, INSERT + " ON CONFLICT DO NOTHING", ps -> {
            int i = 1;
            ps.setObject(i++, permission.getResourceId(), Types.OTHER);
            ps.setString(i++, permission.getRole());
            ps.setInt(i++, pack(permission.getPermissions()));
        });
    }

    @Override
    public void delete(UUID resourceId, String role) throws Exception {
        String delete = "DELETE FROM " + TABLE + " WHERE resource_id = ? AND role = ?";
        JDBC.executeUpdate(ds, delete, ps -> {
            ps.setObject(1, resourceId, Types.OTHER);
            ps.setString(2, role);
        });
    }
    
    private static int pack(Iterable<PermissionType> permissions) {
        int code = 0;
        for (PermissionType permission : permissions) {
            code |= permission.getCode();
        }
        return code;
    }

    private static List<PermissionType> unpack(int code) {
        return Arrays.stream(PermissionType.values())
            .filter(permission -> (permission.getCode() & code) != 0)
            .toList();
    }

}
