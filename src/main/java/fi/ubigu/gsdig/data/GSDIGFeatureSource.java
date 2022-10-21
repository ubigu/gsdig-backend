package fi.ubigu.gsdig.data;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.SimpleInternationalString;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import fi.ubigu.gsdig.arealdivision.ArealDivision;

public class GSDIGFeatureSource extends ContentFeatureSource {

    private final ArealDivision arealDivision;

    public GSDIGFeatureSource(ContentEntry entry, Query query, ArealDivision arealDivision) throws IOException {
        super(entry, query);
        this.arealDivision = arealDivision;
    }

    @Override
    public GSDIGDataStore getDataStore() {
        return (GSDIGDataStore) entry.getDataStore();
    }

    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
        return null;
    }

    @Override
    protected int getCountInternal(Query query) throws IOException {
        return -1;
    }

    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        return new GSDIGFeatureReader(getState(), query, getDataStore().storageSrid);
    }

    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        try {
            SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();

            sftb.setName(arealDivision.getTitle());
            if (arealDivision.getDescription() != null) {
                sftb.setDescription(new SimpleInternationalString(arealDivision.getDescription()));
            }

            String geometryColumn = DataRepositoryPG.GEOM_COLUMN;
            sftb.add(geometryColumn, MultiPolygon.class, getDataStore().storageSrid);
            sftb.setDefaultGeometry(geometryColumn);

            arealDivision.getAttributes().forEach((name, info) -> sftb.add(name, info.getBinding()));
            
            SimpleFeatureType sft = sftb.buildFeatureType();
            String table = DataRepositoryPG.getDataTable(arealDivision.getUuid());
            sft.getUserData().put("table", table);

            return sft;
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }

    @Override
    protected boolean canReproject() {
        return true;
    }

    @Override
    protected boolean canLimit() {
        return true;
    }

    @Override
    protected boolean canOffset() {
        return true;
    }

    @Override
    protected boolean canFilter() {
        return true;
    }

    @Override
    protected boolean canSort() {
        return true;
    }

}
