package fi.ubigu.gsdig.oapif;

import java.util.List;

public class CollectionInfo {
    
    private String id;
    private String title;
    private String description;
    private List<Link> links;
    private Extent extent;
    private List<String> crs;
    private String storageCrs; 

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public Extent getExtent() {
        return extent;
    }

    public void setExtent(Extent extent) {
        this.extent = extent;
    }

    public List<String> getCrs() {
        return crs;
    }

    public void setCrs(List<String> crs) {
        this.crs = crs;
    }

    public String getStorageCrs() {
        return storageCrs;
    }

    public void setStorageCrs(String storageCrs) {
        this.storageCrs = storageCrs;
    }

}
