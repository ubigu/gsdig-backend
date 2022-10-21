package fi.ubigu.gsdig.upload.format;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.geotools.data.simple.SimpleFeatureCollection;

public interface UploadFormat {

    public boolean read(File file, BiConsumer<String, SimpleFeatureCollection> next) throws Exception;
    
    public String getContentType();
    
    public default List<File> getReadableFiles(File file) {
        Stream<File> files;
        if (file.isDirectory()) {
            files = Arrays.stream(file.listFiles());
        } else {
            files = Stream.of(file);
        }

        return files.filter(File::canRead).collect(Collectors.toList());
    }

}
