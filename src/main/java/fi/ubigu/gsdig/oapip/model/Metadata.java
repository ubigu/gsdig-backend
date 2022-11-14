package fi.ubigu.gsdig.oapip.model;

public class Metadata {

    private final String title;
    private final String role;
    private final String href;

    public Metadata(String title, String role, String href) {
        this.title = title;
        this.role = role;
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public String getRole() {
        return role;
    }

    public String getHref() {
        return href;
    }

}
