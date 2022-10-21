package fi.ubigu.gsdig.download.export;

public abstract class BaseFileExporter implements FileExporter {

    private final String fileExtension;
    private final String contentType;

    protected BaseFileExporter(String fileExtension, String contentType) {
        this.fileExtension = fileExtension;
        this.contentType = contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

}
