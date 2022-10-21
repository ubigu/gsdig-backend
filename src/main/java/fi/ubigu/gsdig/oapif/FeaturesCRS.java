package fi.ubigu.gsdig.oapif;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeaturesCRS {

    public static final String CRS84 = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    @Value("${srid.storage}")
    private int storageSrid;

    @Value("${srid.auto}")
    private List<Integer> srids;

    // Lazy
    private List<String> crs;
    private Set<Integer> sridSet;


    public boolean isSupported(int srid) {
        if (sridSet == null) {
            sridSet = new HashSet<>(srids);
        }
        return sridSet.contains(srid);
    }

    public int getStorageSrid() {
        return storageSrid;
    }

    public List<String> getCRS() {
        if (crs == null) {
            List<String> list = new ArrayList<>(srids.size());
            for (int srid : srids) {
                if (srid == 4326) {
                    list.add(CRS84);
                } else {
                    list.add(toEPSGUri(srid));
                }
            }
            crs = list;
        }
        return crs;
    }

    public static String toEPSGUri(int srid) {
        return "http://www.opengis.net/def/crs/EPSG/0/" + srid;
    }

    public static int fromEPSGUri(String epsgUri, int fallback) {
        if (epsgUri.startsWith("http://www.opengis.net/def/crs/EPSG/0/")) {
            String rest = epsgUri.substring("http://www.opengis.net/def/crs/EPSG/0/".length());
            try {
                return Integer.parseInt(rest);
            } catch (NumberFormatException ignore) {
                // Just ignore
            }
        }
        return fallback;
    }

}
