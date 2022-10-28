package fi.ubigu.gsdig.upload.format;

import java.io.File;
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
            String typeName;
            SimpleFeatureCollection collection;
            try {
                typeName =  store.getTypeNames()[0];
                SimpleFeatureSource source = store.getFeatureSource(typeName);
                collection = source.getFeatures();
                if (collection.isEmpty()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            next.accept(typeName, collection);
            return true;
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    @Override
    public String getContentType() {
        return "x-gis/x-shapefile";
    }

}
