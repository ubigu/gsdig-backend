package fi.ubigu.gsdig.oapif;

import java.time.Instant;

public class Extent {

    private final String crs = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
    private double[] spatial;
    private final String trs = "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian";
    private Instant[] temporal;

    public String getCrs() {
        return crs;
    }

    public double[] getSpatial() {
        return spatial;
    }

    public void setSpatial(double[] spatial) {
        this.spatial = spatial;
    }

    public String getTrs() {
        return trs;
    }

    public Instant[] getTemporal() {
        return temporal;
    }

    public void setTemporal(Instant[] temporal) {
        this.temporal = temporal;
    }

}