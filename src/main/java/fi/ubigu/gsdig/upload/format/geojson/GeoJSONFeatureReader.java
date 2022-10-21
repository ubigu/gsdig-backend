package fi.ubigu.gsdig.upload.format.geojson;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.ubigu.gsdig.utility.GeoJSONToGeoTools;

public class GeoJSONFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private final SimpleFeatureType featureType;
    private final GeometryFactory gf;
    private final SimpleFeatureBuilder builder;
    private final GeoJSONFeatureIterator iter;

    public GeoJSONFeatureReader(SimpleFeatureType featureType, Query query, ObjectMapper om, File file) throws JsonParseException, IOException {
        this.featureType = featureType;
        this.gf = new GeometryFactory();
        this.builder = new SimpleFeatureBuilder(featureType);
        this.iter = new GeoJSONFeatureIterator(om, om.createParser(file));
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public SimpleFeature next() throws NoSuchElementException {
        return GeoJSONToGeoTools.toSimpleFeature(gf, builder, iter.next());
    }

    @Override
    public void close() {
        iter.close();
    }

}
