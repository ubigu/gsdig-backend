package fi.ubigu.gsdig.upload;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class ImportCollection {

    private String type;
    private String title;
    private String description;
    private String organization;
    private boolean publicity;
    private Map<String, Object> typeSpecficic;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public boolean isPublicity() {
        return publicity;
    }

    public void setPublicity(boolean publicity) {
        this.publicity = publicity;
    }

    @JsonAnyGetter
    public Map<String, Object> getTypeSpecific() {
        return typeSpecficic;
    }

    @JsonAnySetter
    public void setTypeSpecific(String name, Object value) {
        if (typeSpecficic == null) {
            typeSpecficic = new HashMap<>();
        }
        typeSpecficic.put(name, value);
    }

}
