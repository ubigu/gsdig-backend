package fi.ubigu.gsdig.oapif.model;

public enum FeaturesConformanceClass {

    Core("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core"),
    OpenAPI3("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30"),
    GeoJSON("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson");

    public final String url;

    private FeaturesConformanceClass(String url) {
        this.url = url;
    }

}