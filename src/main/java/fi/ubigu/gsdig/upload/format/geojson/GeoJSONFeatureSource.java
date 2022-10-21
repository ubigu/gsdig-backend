package fi.ubigu.gsdig.upload.format.geojson;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.ubigu.gsdig.utility.GeoJSONToGeoTools;

public class GeoJSONFeatureSource extends ContentFeatureSource {
    
    private static final int LIMIT_SCHEMA_DETECTION = 100;
    
    public GeoJSONFeatureSource(ContentEntry entry, Query query) throws IOException {
        super(entry, query);
    }

    @Override
    public GeoJSONDataStore getDataStore() {
        return (GeoJSONDataStore) entry.getDataStore();
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
        return new GeoJSONFeatureReader(getState().getFeatureType(), query, getDataStore().om, getDataStore().file);
    }

    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        try {
            final ObjectMapper om = getDataStore().om;
            final File file = getDataStore().file;

            SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
            
            CoordinateReferenceSystem crs = getDataStore().crs;
            
            Map<String, Class<?>> bindings;
            try (GeoJSONFeatureIterator iter = new GeoJSONFeatureIterator(om, om.createParser(file))) {
                bindings = GeoJSONToGeoTools.buildSchema(iter, LIMIT_SCHEMA_DETECTION);
                
                if (crs == null) {
                    // This might read the whole file
                    iter.tryToReadFeatureCollectionCrs();
                    crs = GeoJSONToGeoTools.getCRS(iter.featureCollectionCrs);
                }
            }

            sftb.setName(entry.getName());

            Class<?> geometryClass = bindings.remove("geometry");
            if (geometryClass != null) {
                sftb.add("geometry", geometryClass, crs);
                sftb.setDefaultGeometry("geometry");
            }
            
            bindings.forEach(sftb::add);

            return sftb.buildFeatureType();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }
    
    

}
