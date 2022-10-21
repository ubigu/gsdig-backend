package fi.ubigu.gsdig.upload.format;

import java.io.File;
import java.util.List;
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
        List<File> readableFiles = getReadableFiles(file);

        for (File f : readableFiles) {
            CSVDataStore store = null;
            try {
                CSVFileState csvFileState = new CSVFileState(f);
                store = new CSVDataStore(csvFileState, new QgisCSVStrategy(csvFileState));
                String typeName = store.getTypeNames()[0];
                SimpleFeatureSource source = store.getFeatureSource(typeName);
                SimpleFeatureCollection collection = source.getFeatures();
                next.accept(typeName, collection);
                return true;
            } catch (Exception e) {
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

    @Override
    public String getContentType() {
        return "text/csv";
    }

}
