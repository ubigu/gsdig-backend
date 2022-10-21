package fi.ubigu.gsdig.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.WKBReader;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.ubigu.gsdig.utility.RowMapper;
import fi.ubigu.gsdig.utility.Utils;

public class GSDIGFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private static final Logger LOG = LoggerFactory.getLogger(GSDIGFeatureReader.class);

    private final ContentState contentState;
    private final Query query;
    private final FeatureFactory factory;
    private final int storageSrid;

    private Connection c;
    private PreparedStatement ps;
    private ResultSet rs;
    private RowMapper<SimpleFeature> rowMapper;

    private SimpleFeature next;
    private boolean closed = false;

    public GSDIGFeatureReader(ContentState contentState, Query query, int storageSrid) {
        this.contentState = contentState;
        this.query = query;
        this.factory = CommonFactoryFinder.getFeatureFactory(null);
        this.storageSrid = storageSrid;
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return contentState.getFeatureType();
    }

    @Override
    public boolean hasNext() throws IOException {
        if (closed) {
            return false;
        }
        if (next != null) {
            return true;
        }
        try {
            readNext();
            return hasNext();
        } catch (Exception e) {
            close();
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        SimpleFeature swap = next;
        next = null;
        return swap;
    }

    @Override
    public void close() {
        closed = true;
        Utils.closeSilently(rs);
        Utils.closeSilently(ps);
        Utils.closeSilently(c);
    }

    private void readNext() throws Exception {
        if (c == null) {
            initQuery();
        }
        if (!rs.next()) {
            close();
        } else {
            next = rowMapper.map(rs);
        }
    }

    private void initQuery() throws Exception {
        SimpleFeatureType ft = getFeatureType();

        String select = buildSelect(ft);
        LOG.debug(select);

        this.rowMapper = buildRowMapper(ft);
        this.c = ((GSDIGDataStore) contentState.getEntry().getDataStore()).ds.getConnection();
        this.ps = c.prepareStatement(select);
        this.rs = ps.executeQuery();
    }

    private String buildSelect(SimpleFeatureType ft) throws FactoryException {
        CoordinateReferenceSystem crs = query.getCoordinateSystemReproject();
        int epsg = crs == null ? storageSrid : CRS.lookupEpsgCode(crs, false);

        String geomName = ft.getGeometryDescriptor().getLocalName();
        
        String geom;
        if (epsg == storageSrid) {
            geom = "ST_AsBinary(" + geomName + ")";
        } else {
            geom = String.format("ST_AsBinary(ST_Transform(" + geomName + ", %d))", epsg);
        }

        Stream<String> columns = ft.getAttributeDescriptors().stream()
                .filter(it -> !it.getLocalName().equals(geomName))
                .map(it -> it.getLocalName());

        String c = Stream.concat(
                Stream.of("id", geom),
                columns
        ).collect(Collectors.joining(","));

        String select = String.format("SELECT %s FROM %s", c, ft.getUserData().get("table"));

        if (query.getStartIndex() != null) {
            select += " OFFSET " + query.getStartIndex(); 
        }
        if (query.getMaxFeatures() < Integer.MAX_VALUE) {
            select += " LIMIT " + query.getMaxFeatures();
        }

        return select;
    }

    private RowMapper<SimpleFeature> buildRowMapper(SimpleFeatureType ft) {
        List<AttributeDescriptor> attributes = ft.getAttributeDescriptors();
        int n = attributes.size();
        WKBReader wkbReader = new WKBReader();

        return rs -> {
            String id = rs.getString(1);
            Object[] properties = new Object[n];
            properties[0] = toGeometry(wkbReader, rs.getObject(2));
            for (int i = 1; i < n; i++) {
                properties[i] = rs.getObject(i + 2); 
            }
            return factory.createSimpleFeature(properties, ft, id);
        };
    }

    private Geometry toGeometry(WKBReader wkbReader, Object maybeWKB) throws Exception {
        if (maybeWKB == null) {
            return null;
        }
        byte[] buf = (byte[]) maybeWKB;
        return wkbReader.read(buf);
    }

}
