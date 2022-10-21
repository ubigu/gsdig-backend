package fi.ubigu.gsdig.upload.format.geojson;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GeoJSONDataStore extends ContentDataStore {

    protected final File file;
    protected final ObjectMapper om;
    protected final CoordinateReferenceSystem crs;

    public GeoJSONDataStore(File file, ObjectMapper om, CoordinateReferenceSystem crs) {
        this.file = file;
        this.om = om;
        this.crs = crs;
    }
    
    @Override
    protected List<Name> createTypeNames() {
        String typeName = FilenameUtils.getBaseName(file.getPath());
        return Collections.singletonList(new NameImpl(typeName));
    }

    @Override
    protected GeoJSONFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new GeoJSONFeatureSource(entry, Query.ALL);
    }

}
