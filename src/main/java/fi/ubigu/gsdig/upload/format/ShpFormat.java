package fi.ubigu.gsdig.upload.format;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class ShpFormat implements UploadFormat {

    @Override
    public boolean read(File file, BiConsumer<String, SimpleFeatureCollection> next) throws Exception {
        List<File> readableFiles = getReadableFiles(file);
        File shpFile = readableFiles.stream()
                .filter(f -> f.getName().toLowerCase().endsWith(".shp"))
                .findFirst()
                .orElse(null);
        if (shpFile == null) {
            return false;
        }
        
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", shpFile.toURI().toURL());

        DataStore store = null;
        try {
            store = DataStoreFinder.getDataStore(params);
            String typeName =  store.getTypeNames()[0];
            SimpleFeatureSource source = store.getFeatureSource(typeName);
            SimpleFeatureCollection collection = source.getFeatures();
            next.accept(typeName, collection);
            return true;
        } catch (IOException e) {
            // Just ignore it
            e.printStackTrace();
        } finally {
            if (store != null) {
                store.dispose();
            }
        }

        return false;
    }

    @Override
    public String getContentType() {
        return "x-gis/x-shapefile";
    }

}
