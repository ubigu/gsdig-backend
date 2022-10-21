package fi.ubigu.gsdig.data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.geojson.Feature;
import org.geotools.data.simple.SimpleFeatureCollection;

import fi.ubigu.gsdig.upload.UploadInfo;

public interface DataRepository {

    public UploadInfo create(SimpleFeatureCollection collection) throws Exception;

    public void setSrid(UUID uuid, int srid) throws Exception;
    public void transform(UUID uuid, int srid) throws Exception;
    public Map<String, String> validate(UUID uuid) throws Exception;

    public void drop(UUID uuid) throws Exception;
    
    public void rename(UUID src, UUID dst) throws Exception;
    
    public void clone(UUID src, UUID dst) throws Exception;
    public int delete(UUID uuid, long featureId) throws Exception;
    
    public void removeAttribute(UUID uuid, String column) throws Exception;

    public void batchUpdate(UUID uuid, List<Feature> tx) throws Exception;


    // public long merge(UUID uuid, long[] featureIds, Map<String, Object> newProperties) throws Exception;

}
