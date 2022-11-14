package fi.ubigu.gsdig.oapif.client;

import java.io.IOException;

import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.ubigu.gsdig.oapif.CRSUtil;
import fi.ubigu.gsdig.oapif.FeatureCollectionResponse;
import fi.ubigu.gsdig.oapif.model.CollectionInfo;
import fi.ubigu.gsdig.utility.GeoJSONToGeoTools;

public class OAPIFFeatureSource extends ContentFeatureSource {

    private static final int SCHEMA_LIMIT = 20;
    private static final int PAGE_SIZE = 5000;

    public OAPIFFeatureSource(ContentEntry entry) {
        super(entry, null);
    }

    @Override
    public OAPIFDataStore getDataStore() {
        return (OAPIFDataStore) super.getDataStore();
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
        if (Filter.EXCLUDE.equals(query.getFilter())) {
            return new EmptyFeatureReader<>(getSchema());
        }

        HttpClient http = getDataStore().http;
        String root = getDataStore().root;

        String collectionId = getEntry().getName().getLocalPart();
        CollectionInfo collection = getDataStore().getCollection(collectionId);

        int limit = PAGE_SIZE;
        // This FeatureSource can't reproject
        String requestCrs = collection.getStorageCrs();

        String itemsURL = FeaturesClient.getItemsURL(root, collectionId, limit, requestCrs);

        return new OAPIFFeatureReader(http, getSchema(), itemsURL);
    }

    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        String collectionId = getEntry().getName().getLocalPart();
        CollectionInfo collection = getDataStore().getCollection(collectionId);
        
        CoordinateReferenceSystem storageCrs = getCRS(collection.getStorageCrs());

        String root = getDataStore().root;
        String itemsURL = FeaturesClient.getItemsURL(root, collectionId, SCHEMA_LIMIT, collection.getStorageCrs());

        HttpClient http = getDataStore().http;
        FeatureCollectionResponse fc = FeaturesClient.getFeatures(http, itemsURL);
        return GeoJSONToGeoTools.buildSchema(fc.getFeatures(), storageCrs, collectionId);
    }

    private CoordinateReferenceSystem getCRS(String crsURL) throws IOException {
        try {
            String code = "EPSG:" + CRSUtil.parseSRID(crsURL);
            code = crsURL;
            // GeoJSON always lon, lat[, alt][, ...]
            boolean longitudeFirst = true;
            return CRS.decode(code, longitudeFirst);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
