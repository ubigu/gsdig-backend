package fi.ubigu.gsdig.oapif.client;

import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import fi.ubigu.gsdig.oapif.FeatureCollectionResponse;
import fi.ubigu.gsdig.utility.GeoJSONToGeoTools;

public class OAPIFFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {
    
    private final HttpClient client;
    private final SimpleFeatureType schema;
    private final SimpleFeatureBuilder builder;
    private final GeometryFactory gf;

    private String itemsURL;
    private Iterator<SimpleFeature> iterator;
    private boolean closed;

    public OAPIFFeatureReader(HttpClient client, SimpleFeatureType schema, String itemsURL) {
        this.client = client;
        this.schema = schema;
        this.builder = new SimpleFeatureBuilder(schema);
        this.gf = new GeometryFactory();
        this.itemsURL = itemsURL;
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return schema;
    }
    
    @Override
    public boolean hasNext() {
        if (closed) {
            return false;
        }
        if (iterator != null && iterator.hasNext()) {
            return true;
        }
        readNextPage();
        return hasNext();
    }

    private void readNextPage() {
        if (itemsURL == null) {
            close();
            return;
        }

        try {
            FeatureCollectionResponse response = FeaturesClient.getFeatures(client, itemsURL);
            itemsURL = FeaturesClient.getNextURL(response);
            if (!response.getFeatures().isEmpty()) {
                iterator = response.getFeatures().stream()
                        .map(f -> GeoJSONToGeoTools.toSimpleFeature(gf, builder, f))
                        .iterator();
            }
        } catch (Exception e) {
            close();
        }
    }

    @Override
    public SimpleFeature next() {
        return iterator.next();
    }

    @Override
    public void close() {
        closed = true;
    }

}
