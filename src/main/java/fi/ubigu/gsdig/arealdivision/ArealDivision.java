package fi.ubigu.gsdig.arealdivision;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.ubigu.gsdig.DatasetMetadata;

public class ArealDivision extends DatasetMetadata {

    protected final UUID uuid;
    protected final UUID createdBy;
    
    public ArealDivision(UUID uuid, UUID createdBy, String title, String description, String organization, boolean publicity,
            double[] extent, Map<String, AttributeInfo> attributes) {
        super(title, description, organization, publicity, extent, attributes);
        this.uuid = uuid;
        this.createdBy = createdBy;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    @JsonIgnore
    public UUID getCreatedBy() {
        return createdBy;
    }

    public ArealDivision withUuid(UUID randomUUID) {
        return new ArealDivision(randomUUID, createdBy, title, description, organization, publicity, extent, attributes);
    }

    public ArealDivision withCreatedBy(UUID userId) {
        return new ArealDivision(uuid, userId, title, description, organization, publicity, extent, attributes);
    }

    public ArealDivision withTitle(String title) {
        return new ArealDivision(uuid, createdBy, title, description, organization, publicity, extent, attributes);
    }

    public ArealDivision withPublicity(boolean b) {
        return new ArealDivision(uuid, createdBy, title, description, organization, b, extent, attributes);
    }

}
