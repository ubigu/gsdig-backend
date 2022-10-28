package fi.ubigu.gsdig.upload.format;

import java.io.File;
import java.util.function.BiConsumer;

import org.geotools.data.csv.CSVDataStore;
import org.geotools.data.csv.CSVFileState;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import fi.ubigu.gsdig.upload.format.csv.QgisCSVStrategy;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class CSVFormat implements UploadFormat {

    @Override
    public boolean read(File file, BiConsumer<String, SimpleFeatureCollection> next) throws Exception {
        for (File f : getReadableFiles(file)) {
            CSVDataStore store = null;
            try {
                String typeName;
                SimpleFeatureCollection collection;
                try {
                    CSVFileState csvFileState = new CSVFileState(f);
                    store = new CSVDataStore(csvFileState, new QgisCSVStrategy(csvFileState));
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
        return "text/csv";
    }

}
