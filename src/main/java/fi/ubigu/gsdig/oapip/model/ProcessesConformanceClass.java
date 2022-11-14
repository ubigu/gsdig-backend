package fi.ubigu.gsdig.oapip.model;

public enum ProcessesConformanceClass {

    Core("http://www.opengis.net/spec/ogcapi-processes-1/1.0/conf/core"),
    OpenAPI3("http://www.opengis.net/spec/ogcapi-processes-1/1.0/conf/oas30"),
    JSON("http://www.opengis.net/spec/ogcapi-processes-1/1.0/conf/json"),
    ProcessDescription("http://www.opengis.net/spec/ogcapi-processes-1/1.0/conf/ogc-process-description");
    
    public final String url;

    private ProcessesConformanceClass(String url) {
        this.url = url;
    }

}