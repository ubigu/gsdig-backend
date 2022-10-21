package fi.ubigu.gsdig.download;

public class DownloadFile {

    private final String path;
    private final String contentType;
    private final String filename;
    private final long length;

    public DownloadFile(String path, String contentType, String filename, long length) {
        this.path = path;
        this.contentType = contentType;
        this.filename = filename;
        this.length = length;
    }

    public String getPath() {
        return path;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFilename() {
        return filename;
    }

    public long getLength() {
        return length;
    }

}
