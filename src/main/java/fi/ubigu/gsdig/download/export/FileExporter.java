package fi.ubigu.gsdig.download.export;

import java.io.File;

import org.geotools.data.simple.SimpleFeatureCollection;

public interface FileExporter {

    public String getFileExtension();
    public String getContentType();
    public File export(File dir, SimpleFeatureCollection featureCollection) throws Exception;

}
