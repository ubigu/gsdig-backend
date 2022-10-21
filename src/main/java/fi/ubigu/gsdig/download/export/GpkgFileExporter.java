package fi.ubigu.gsdig.download.export;

import java.io.File;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.springframework.stereotype.Component;

@Component("gpkg")
public class GpkgFileExporter extends BaseFileExporter implements FileExporter {

    public GpkgFileExporter() {
        super(".gpkg", "application/geopackage+sqlite3");
    }

    @Override
    public File export(File dir, SimpleFeatureCollection featureCollection) throws Exception {
        File file = File.createTempFile("tmp", ".gpkg", dir);

        try (GeoPackage gpkg = new GeoPackage(file)) {
            gpkg.init();

            FeatureEntry entry = new FeatureEntry();
            entry.setDescription(featureCollection.getID());
            gpkg.add(entry, featureCollection);
            // This just fails?
            // gpkg.createSpatialIndex(entry);
        }

        return file;
    }

}
