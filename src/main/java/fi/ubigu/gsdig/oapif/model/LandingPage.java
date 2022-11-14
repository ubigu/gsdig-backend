package fi.ubigu.gsdig.oapif.model;

import java.util.List;

import fi.ubigu.gsdig.oapi.model.Link;

public class LandingPage {

    private String title;
    private String description;
    private List<Link> links;

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

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

}
