package fi.ubigu.gsdig.oapip.model;

import java.util.List;

public class DescriptionType {

    private final String title;
    private final String description;
    private final List<String> keywords;
    private final List<Metadata> metadata;

    public DescriptionType(String title, String description, List<String> keywords, List<Metadata> metadata) {
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.metadata = metadata;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

}
