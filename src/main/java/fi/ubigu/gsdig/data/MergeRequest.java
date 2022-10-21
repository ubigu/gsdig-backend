package fi.ubigu.gsdig.data;

import java.util.Map;

public class MergeRequest {

    private long[] featureIds;
    private Map<String, Object> properties;

    public long[] getFeatureIds() {
        return featureIds;
    }

    public void setFeatureIds(long[] featureIds) {
        this.featureIds = featureIds;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
