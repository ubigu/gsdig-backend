package fi.ubigu.gsdig.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.geojson.Feature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
import org.geotools.referencing.CRS.AxisOrder;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.ubigu.gsdig.upload.UploadInfo;
import fi.ubigu.gsdig.utility.GeoJSONToGeoTools;
import fi.ubigu.gsdig.utility.JDBC;
import fi.ubigu.gsdig.utility.PGUtil;
import fi.ubigu.gsdig.utility.PostGISFeatureWriter;

@Component
public class DataRepositoryPG implements DataRepository {
    
    public static final String DATA_TABLE_PREFIX = "data_";
    public static final String GEOM_COLUMN = "geom";

    public static String getDataTable(UUID uuid) {
        return DATA_TABLE_PREFIX + uuid.toString().replace('-', '_');
    }
    
    @Autowired
    private DataSource ds;
    
    @Value("${srid.storage}")
    private int storageSRID;

    @Autowired
    private ProjectionRecognizer projectionRecognizer;

    @Override
    public UploadInfo create(SimpleFeatureCollection collection) throws Exception {
        UUID uuid = UUID.randomUUID();

        String dataTable = getDataTable(uuid);
        String geometryColumn = GEOM_COLUMN;

        SimpleFeatureType schema = collection.getSchema();
        GeometryDescriptor geometryDescriptor = schema.getGeometryDescriptor();

        List<AttributeDescriptor> attributes = getAttributes(schema);
        Map<String, Class<?>> attrs = attributes.stream()
                .collect(Collectors.toMap(a -> a.getLocalName(), a -> a.getType().getBinding()));

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);

            Envelope extent = PostGISFeatureWriter.insertData(c, collection, dataTable, geometryColumn, attributes);

            int srid = detectSrid(c, geometryDescriptor, extent);

            if (srid < 0) {
                // flipped axis order
                flipCoordinates(c, dataTable, geometryColumn);
                srid = -srid;
            }

            setSrid(c, dataTable, geometryColumn, srid);
            if (storageSRID != srid) {
                try {
                    transform(c, dataTable, geometryColumn, storageSRID);
                } catch (Exception e) {
                    // Try flipping the axis order one more time
                    try {
                        flipCoordinates(c, dataTable, geometryColumn);
                        transform(c, dataTable, geometryColumn, storageSRID);
                    } catch (Exception e2) {
                        throw e;
                    }
                    // Flipping it the second time worked, just roll with it
                }
                extent = getExtent(c, dataTable, geometryColumn);
            }

            c.commit();

            double[] xtent = {
                    extent.getMinX(), extent.getMinY(),
                    extent.getMaxX(), extent.getMaxY()
            }; 

            return new UploadInfo(uuid, null, xtent, srid, attrs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<AttributeDescriptor> getAttributes(SimpleFeatureType schema) {
        GeometryDescriptor geometryDescriptor = schema.getGeometryDescriptor();
        Stream<AttributeDescriptor> attributes = schema.getAttributeDescriptors().stream();
        if (geometryDescriptor != null) {
            attributes = attributes.filter(it -> !it.getName().equals(geometryDescriptor.getName()));
        }
        return attributes.filter(it -> PGUtil.getPostgresType(it.getType().getBinding()) != null)
                .collect(Collectors.toList());
    }

    private int detectSrid(Connection c, GeometryDescriptor geometryDescriptor, Envelope extent) throws Exception {
        CoordinateReferenceSystem crs = geometryDescriptor.getCoordinateReferenceSystem();
        if (crs != null) {
            int f = CRS.getAxisOrder(crs) == AxisOrder.NORTH_EAST ? -1 : 1;
            try {
                Integer code = CRS.lookupEpsgCode(crs, false);
                if (code != null) {
                    return f * code;
                }
            } catch (Exception ignore) {
                // Just ignore it
            }

            try {
                Integer code = CRS.lookupEpsgCode(crs, true);
                if (code != null) {
                    return f * code;
                }
            } catch (Exception ignore) {
                throw new Exception("Could not find epsg code for recognized CRS");
                // Just ignore it
            }
        }
        return projectionRecognizer.getSRID(extent);
    }
    
    private Envelope getExtent(Connection c, String table, String geometryColumn) throws Exception {
        String select = String.format("SELECT ST_AsBinary(ST_Extent(%s)) FROM %s", geometryColumn, table);
        return JDBC.findFirst(c, select, rs -> {
            try {
                Geometry extent = new WKBReader().read(rs.getBytes(1));
                return extent.getEnvelopeInternal();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).orElseThrow();
    }
    
    @Override
    public void setSrid(UUID uuid, int srid) throws Exception {
        try (Connection c = ds.getConnection()) {
            setSrid(c, getDataTable(uuid), GEOM_COLUMN, srid);
        }
    }

    private void flipCoordinates(Connection c, String table, String geometryColumn) throws Exception {
        String update = String.format(
                "UPDATE %s SET %s = ST_FlipCoordinates(%s)",
                table, geometryColumn, geometryColumn);
        JDBC.executeUpdate(c, update);
    }

    private void setSrid(Connection c, String table, String geometryColumn, int srid) throws Exception {
        String alterTable = String.format(""
                + "ALTER TABLE %s "
                + "ALTER COLUMN %s TYPE geometry(%s, %d) "
                + "USING ST_SetSRID(%s, %d)",
                table,
                geometryColumn, "geometry", srid,
                geometryColumn, srid);
        JDBC.executeUpdate(c, alterTable);
    }

    @Override
    public void transform(UUID uuid, int srid) throws Exception {
        try (Connection c = ds.getConnection()) {
            transform(c, getDataTable(uuid), GEOM_COLUMN, srid);
        }
    }
    
    private void transform(Connection c, String table, String geometryColumn, int srid) throws Exception {
        String alterTable = String.format(""
                + "ALTER TABLE %s "
                + "ALTER COLUMN %s TYPE geometry(%s, %d) "
                + "USING ST_Transform(%s, %d)",
                table,
                geometryColumn, "geometry", srid,
                geometryColumn, srid);
        JDBC.executeUpdate(c, alterTable);
    }

    @Override
    public Map<String, String> validate(UUID uuid) throws Exception {
        String select = String.format(""
                + "SELECT id, reason(ST_IsValidDetail(%s)) "
                + "FROM %s "
                + "WHERE ST_IsValid(%s) = false",
                GEOM_COLUMN,
                getDataTable(uuid),
                GEOM_COLUMN);

        return JDBC.findResultset(ds, select, JDBC.NOP, rs -> {
            Map<String, String> map = new HashMap<>();
            while (rs.next()) {
                String id = rs.getString(1);
                String reason = rs.getString(2);
                map .put(id, reason);
            }
            return map;
        });
    }

    @Override
    public void drop(UUID uuid) throws Exception {
        JDBC.executeUpdate(ds, "DROP TABLE IF EXISTS " + getDataTable(uuid));
    }

    @Override
    public void rename(UUID src, UUID dst) throws Exception {
        String s = getDataTable(src);
        String d = getDataTable(dst);
        String alterTable = String.format("ALTER TABLE %s RENAME TO %s", s, d);
        JDBC.executeUpdate(ds, alterTable);
    }

    @Override
    public void clone(UUID src, UUID dst) throws Exception {
        String s = getDataTable(src);
        String d = getDataTable(dst);
        String createTable = String.format("CREATE TABLE %s (LIKE %s INCLUDING ALL)", d, s);
        String insert = String.format("INSERT INTO %s TABLE %s", d, s);
        String setval = String.format("SELECT setval(pg_get_serial_sequence('%s', 'id'), max(id)) FROM %s", d, s);
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            JDBC.executeUpdate(c, createTable);
            JDBC.executeUpdate(c, insert);
            JDBC.findFirst(c, setval, __ -> 1);
            c.commit();
        }
    }

    @Override
    public int delete(UUID uuid, long featureId) throws Exception {
        return JDBC.executeUpdate(ds,
                "DELETE FROM " + getDataTable(uuid) + " WHERE id = ?",
                ps -> ps.setLong(1, featureId));
    }

    @Override
    public void removeAttribute(UUID uuid, String column) throws Exception {
        String dropColumn = String.format(
                "ALTER TABLE %s DROP COLUMN %s",
                getDataTable(uuid), column);
        JDBC.executeUpdate(ds, dropColumn);
    }

    @Override
    public void batchUpdate(UUID uuid, List<Feature> tx) throws Exception {
        // Make sure iteration order is fixed
        List<String> props = tx.get(0).getProperties() != null ? new ArrayList<>(tx.get(0).getProperties().keySet()) : Collections.emptyList();
        List<String> columns = new ArrayList<>(1 + props.size());
        columns.add(GEOM_COLUMN);
        columns.addAll(props);
        
        String update = String.format("UPDATE %s SET %s WHERE id = ?", 
                getDataTable(uuid),
                columns.stream()
                    .map(column -> column + " = ?")
                    .collect(Collectors.joining(", "))
        );
        
        GeometryFactory gf = new GeometryFactory();
        WKBWriter wkb = new WKBWriter();
        
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(update)) {
                for (Feature f : tx) {
                    int i = 1;
                    Geometry jts = GeoJSONToGeoTools.toGeometry(gf, f.getGeometry());
                    ps.setBytes(i++, wkb.write(jts));
                    for (String prop : props) {
                        ps.setObject(i++, f.getProperty(prop));
                    }
                    ps.setLong(i, Long.parseLong(f.getId()));
                    ps.addBatch();
                }
                ps.executeBatch();
            };
            c.commit();
        }
    }
    
    /*
    @Override
    public long merge(UUID uuid, long[] featureIds, Map<String, Object> newProperties) throws Exception {
        long id;

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            String table = getDataTable(uuid);
            int count = selectCountWhereIds(c, table, featureIds);
            if (count != featureIds.length) {
                throw new IllegalArgumentException("All requested featureIds must exists");
            }
            id = mergeInsert(c, table, newProperties, featureIds);
            insertMergeHistory(c, uuid, featureIds, id);
            moveToMergeHistoryData(c, table, featureIds);
            c.commit();
        }

        return id;
    }
    
    private int selectCountWhereIds(Connection c, String table, long[] featureIds) throws Exception {
        String select = "SELECT COUNT(*) FROM " + table + " WHERE id = any(?)";
        return JDBC.findFirst(c, select,
                ps -> ps.setObject(1, featureIds),
                rs -> rs.getInt(1)
        ).orElse(0);
    }
    
    private long mergeInsert(Connection c, String table, long[] featureIds, Map<String, Object> newProperties) throws Exception {
        String[] propertyNames = newProperties.keySet().stream().toArray(String[]::new);

        String columns = Stream.concat(Stream.of(GEOM_COLUMN), Arrays.stream(propertyNames))
                .collect(Collectors.joining(","));

        String selectColumns = Stream.concat(
                Stream.of("ST_Multi(ST_Union(" + GEOM_COLUMN + "))"),
                Stream.generate(() -> "?").limit(propertyNames.length)
        ).collect(Collectors.joining(","));

        String insert = String.format("INSERT INTO %s (%s) "
                + "SELECT %s FROM %s WHERE id = any(?)",
                table, columns,
                selectColumns, table);

        try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            for (String propertyName : propertyNames) {
                Object o = newProperties.get(propertyName);
                ps.setObject(i++, o);
            }
            ps.setObject(i++, featureIds);

            int affectedRows = ps.executeUpdate();
            if (affectedRows != 1) {
                throw new RuntimeException("Failed to insert merged feature");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            throw new RuntimeException("Failed to fetch generated key");
        }
    }
     */

}
