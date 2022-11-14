package fi.ubigu.gsdig.oapif.model;

import java.util.List;

import fi.ubigu.gsdig.oapi.model.Link;

public class CollectionsInfo {

    private List<Link> links;
    private List<CollectionInfo> collections;

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<CollectionInfo> getCollections() {
        return collections;
    }

    public void setCollections(List<CollectionInfo> collections) {
        this.collections = collections;
    }

}
