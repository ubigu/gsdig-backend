package fi.ubigu.gsdig.oapif;

import java.util.List;
import java.util.Map;

import org.geojson.Crs;
import org.geojson.GeoJsonObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder("type")
public class FeatureResponse {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Map<String, Object> properties;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private GeoJsonObject geometry;
    private Crs crs;
    private String id;
    private List<Link> links;

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public GeoJsonObject getGeometry() {
        return geometry;
    }

    public void setGeometry(GeoJsonObject geometry) {
        this.geometry = geometry;
    }

    public Crs getCrs() {
        return crs;
    }

    public void setCrs(Crs crs) {
        this.crs = crs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

}
