package fi.ubigu.gsdig.oapif;

import java.time.Instant;
import java.util.List;

import org.geojson.Feature;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.ubigu.gsdig.oapi.model.Link;

@JsonPropertyOrder({ "type", "numberReturned", "timeStamp", "links", "features" })
public class FeatureCollectionResponse {

    private List<Feature> features;
    private List<Link> links;
    private int numberReturned;
    private Instant timeStamp;

    @JsonProperty("type")
    public String getType() {
        return "FeatureCollection";
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public int getNumberReturned() {
        return numberReturned;
    }

    public void setNumberReturned(int numberReturned) {
        this.numberReturned = numberReturned;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

}
