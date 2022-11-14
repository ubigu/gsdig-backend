package fi.ubigu.gsdig.oapip.model;

public class ProcessesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    
    public static ProcessesException noSuchProcess() {
        return new ProcessesException("http://www.opengis.net/def/exceptions/ogcapi-processes-1/1.0/no-such-process", "No such process", 404, null, null);
    }
    
    public static ProcessesException noSuchJob() {
        return new ProcessesException("http://www.opengis.net/def/exceptions/ogcapi-processes-1/1.0/no-such-job", "No such job", 404, null, null);
    }

    public ProcessesException(String type, String title, int status, String detail, String instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public String getInstance() {
        return instance;
    }

}
