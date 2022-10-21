package fi.ubigu.gsdig.upload;

import java.util.Map;
import java.util.UUID;

public class UploadInfo {

    private final UUID uuid;
    private final String typeName;
    private final double[] extent;
    private final int srid;
    private final Map<String, Class<?>> attributes;

    public UploadInfo(UUID uuid,
            String typeName,
            double[] extent,
            int srid,
            Map<String, Class<?>> attributes) {
        this.uuid = uuid;
        this.typeName = typeName;
        this.extent = extent;
        this.srid = srid;
        this.attributes = attributes;
    }
    
    public UUID getUuid() {
        return uuid;
    }

    public double[] getExtent() {
        return extent;
    }

    public int getSrid() {
        return srid;
    }

    public Map<String, Class<?>> getAttributes() {
        return attributes;
    }

    public String getTypeName() {
        return typeName;
    }

    public UploadInfo withTypeName(String typeName) {
        return new UploadInfo(uuid, typeName, extent, srid, attributes);
    }

}
