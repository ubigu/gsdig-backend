package fi.ubigu.gsdig.upload.format;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import fi.ubigu.gsdig.utility.Utils;

@Component
@Order(0)
public class GPKGFormat implements UploadFormat {

    private static final byte[] SQLITE_HEADER = new byte[] { 0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x20, 0x33, 0x00 };

    @Override
    public boolean read(File file, BiConsumer<String, SimpleFeatureCollection> next) {
        List<File> sqliteFiles = getReadableFiles(file).stream()
                .filter(it -> isSqliteFile(it))
                .collect(Collectors.toList());

        for (File sqliteFile : sqliteFiles) {
            Map<String, Serializable> params = new HashMap<>();
            params.put("dbtype", "geopkg");
            params.put("database", sqliteFile);
            DataStore store = null;
            try {
                store = DataStoreFinder.getDataStore(params);
                for (String typeName : store.getTypeNames()) {
                    SimpleFeatureSource source = store.getFeatureSource(typeName);
                    SimpleFeatureCollection collection = source.getFeatures();
                    next.accept(typeName, collection);
                }
                return true;
            } catch (IOException e) {
                // Just ignore it
                e.printStackTrace();
            } finally {
                if (store != null) {
                    store.dispose();
                }
            }
        }

        return false;
    }

    private boolean isSqliteFile(File file) {
        try {
            byte[] header = Utils.readFirstNBytes(file, SQLITE_HEADER.length);
            return Arrays.equals(header, SQLITE_HEADER);
        } catch (Exception ignore) {
            return false;
        }
    }

    @Override
    public String getContentType() {
        return "application/geopackage+sqlite3";
    }

}
