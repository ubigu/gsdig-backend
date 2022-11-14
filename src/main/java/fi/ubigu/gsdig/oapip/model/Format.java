package fi.ubigu.gsdig.oapip.model;

public class Format {

    private final String mediaType;
    private final String encoding;
    private final String schema;

    public Format(String mediaType, String encoding, String schema) {
        this.mediaType = mediaType;
        this.encoding = encoding;
        this.schema = schema;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getSchema() {
        return schema;
    }

}
