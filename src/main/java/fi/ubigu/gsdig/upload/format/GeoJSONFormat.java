package fi.ubigu.gsdig.upload.format;

import java.io.File;
import java.util.function.BiConsumer;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.ubigu.gsdig.upload.format.geojson.GeoJSONDataStore;

@Component
@Order(0)
public class GeoJSONFormat implements UploadFormat {

    @Autowired
    private ObjectMapper om;

    @Override
    public boolean read(File file, BiConsumer<String, SimpleFeatureCollection> next) {
        for (File readableFile : getReadableFiles(file)) {
            DataStore store = null;
            try {
                String typeName;
                SimpleFeatureCollection collection;
                try {
                    store = new GeoJSONDataStore(readableFile, om, null);
                    typeName = store.getTypeNames()[0];
                    SimpleFeatureSource source = store.getFeatureSource(typeName);
                    collection = source.getFeatures();
                    if (collection.isEmpty()) {
                        continue;
                    }
                } catch (Exception ignore) {
                    continue;
                }
                next.accept(typeName, collection);
                return true;
            } finally {
                if (store != null) {
                    store.dispose();
                }
            }
        }
        return false;
    }

    @Override
    public String getContentType() {
        return "application/geo+json";
    }

}
