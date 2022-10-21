package fi.ubigu.gsdig.oapif;

public class CRSUtil {

    public static final String URI_PREFIX = "http://www.opengis.net/def/crs/";
    public static final String ERR_MODEL = "CRS shall follow format model 'http://www.opengis.net/def/crs/{authority}/{version}/{code}'";
    public static final String ERR_UNKNOWN_CRS = "Unrecognized coordinate reference system";
    public static final String ERR_UNSUPPORTED_CRS = "Coordinate reference system not supported";

    public static final int CRS84_SRID = 84;
    public static final String CRS84 = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    public static int parseSRID(String crsUri) throws IllegalArgumentException {
        if (crsUri == null || crsUri.isEmpty()) {
            return CRS84_SRID;
        }
        if (CRS84.equals(crsUri)) {
            return CRS84_SRID;
        }

        if (!crsUri.startsWith(URI_PREFIX)) {
            throw new IllegalArgumentException(ERR_MODEL);
        }
        crsUri = crsUri.substring(URI_PREFIX.length());

        int i = crsUri.indexOf('/');
        if (i < 0) {
            throw new IllegalArgumentException(ERR_MODEL);
        }

        // String authority = crsUri.substring(0, i);
        crsUri = crsUri.substring(i + 1);

        i = crsUri.indexOf('/');
        if (i < 0) {
            throw new IllegalArgumentException(ERR_MODEL);
        }

        // String version = crsUri.substring(0, i);

        String code = crsUri.substring(i + 1);
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERR_UNKNOWN_CRS);
        }
    }

    public static String toUri(int srid) {
        if (srid == CRS84_SRID) {
            return CRS84;
        }
        return "http://www.opengis.net/def/crs/EPSG/0/" + srid;
    }

}
