package fi.ubigu.gsdig;

import java.util.Map;

import fi.ubigu.gsdig.arealdivision.AttributeInfo;

public class DatasetMetadata {

    protected final String title;
    protected final String description;
    protected final String organization;
    protected final boolean publicity;
    protected final double[] extent;
    protected final Map<String, AttributeInfo> attributes;
    
    public DatasetMetadata(String title, String description, String organization, boolean publicity,
            double[] extent, Map<String, AttributeInfo> attributes) {
        this.title = title;
        this.description = description;
        this.organization = organization;
        this.publicity = publicity;
        this.extent = extent;
        this.attributes = attributes;
    }
    
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getOrganization() {
        return organization;
    }

    public boolean isPublicity() {
        return publicity;
    }

    public double[] getExtent() {
        return extent;
    }

    public Map<String, AttributeInfo> getAttributes() {
        return attributes;
    }

}
