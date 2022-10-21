package fi.ubigu.gsdig.upload.format.geojson;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geojson.Crs;
import org.geojson.Feature;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class GeoJSONFeatureIterator implements Iterator<Feature>, AutoCloseable {
    
    private final JsonParser parser;
    private final ObjectReader featureReader;
    private final ObjectReader crsReader;

    public Crs featureCollectionCrs;
    public boolean featureCollectionCrsRead; // read crs might be null

    private boolean featureArrayReached;
    private boolean closed;
    private Feature next;

    public GeoJSONFeatureIterator(ObjectMapper om, JsonParser parser) {
        this.parser = parser;
        this.featureReader = om.readerFor(Feature.class);
        this.crsReader = om.readerFor(Crs.class);
        this.closed = false;
        this.next = null;
    }

    @Override
    public boolean hasNext() {
        if (closed) {
            return false;
        }
        if (next != null) {
            return true;
        }
        try {
            if (!featureArrayReached) {
                if (!readUntilStartFeaturesArray()) {
                    close();
                    return false;
                }
                featureArrayReached = true;
            }
            readNext();
        } catch (Exception e) {
            close();
        }

        return hasNext();
    }

    private boolean readUntilStartFeaturesArray() throws IOException {
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (isStartFeaturesArray(token)) {
                return true;
            } else if (isStartCrsObject(token)) {
                featureCollectionCrs = crsReader.readValue(parser);
                featureCollectionCrsRead = true;
            }
        }
        return false;
    }
    
    private final boolean isStartFeaturesArray(JsonToken token) throws IOException {
        return token == JsonToken.START_ARRAY && "features".equals(parser.getCurrentName());
    }
    
    private final boolean isStartCrsObject(JsonToken token) throws IOException {
        return token == JsonToken.START_OBJECT && "crs".equals(parser.getCurrentName());
    }

    private void readNext() throws IOException {
        JsonToken token = parser.nextToken();
        if (token == JsonToken.END_ARRAY) {
            // features array ended
            tryToReadFeatureCollectionCrs();
            close();
        } else if (token == JsonToken.START_OBJECT) {
            next = featureReader.readValue(parser);
        }
    }
    
    public void tryToReadFeatureCollectionCrs() throws IOException {
        if (closed || featureCollectionCrsRead) {
            return;
        }
        
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (isStartCrsObject(token)) {
                featureCollectionCrs = crsReader.readValue(parser);
                featureCollectionCrsRead = true;
                break;
            }
        }
    }
    
    @Override
    public Feature next() {
        if (closed || next == null) {
            throw new NoSuchElementException();
        }
        Feature swap = next;
        next = null;
        return swap;
    }

    @Override
    public void close() {
        closed = true;
        try {
            parser.close();
        } catch (Exception ignore) {
            
        }
    }

    public Crs getFeatureCollectionCrs() {
        return featureCollectionCrs;
    }

    public void setFeatureCollectionCrs(Crs featureCollectionCrs) {
        this.featureCollectionCrs = featureCollectionCrs;
    }

}
