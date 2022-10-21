package fi.ubigu.gsdig.oapif;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeaturesRequestParser {

    @Autowired
    private FeaturesCRS featureCrs;
    
    public List<String> getCrs() {
        return featureCrs.getCRS();
    }
    
    public String getStorageCrs() {
        return FeaturesCRS.toEPSGUri(featureCrs.getStorageSrid());
    }

    public GetItemsRequest parse(String collectionId, String bbox, String datetime, int offset, int limit, String crs, String bboxCrs) {
        double[] _bbox = null;
        if (bbox != null && !bbox.isEmpty()) {
            _bbox = Arrays.stream(bbox.split(","))
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            if (!(_bbox.length == 4 || _bbox.length == 6)) {
                throw new IllegalArgumentException("Parameter value '" + bbox + "' is invalid for parameter 'bbox'");
            }
        }

        int srid = getSrid(crs, "crs");
        int bboxSrid = getSrid(bboxCrs, "bbox-crs");

        return new GetItemsRequest(collectionId, _bbox, bboxSrid, offset, limit, srid);
    }

    public int getSrid(String crs, String field) {
        if (crs != null && !crs.isEmpty()) {
            int srid = FeaturesCRS.fromEPSGUri(crs, -1);
            if (srid == -1 || !featureCrs.isSupported(srid)) {
                throw new IllegalArgumentException("Parameter value '" + crs + "' is invalid for parameter '" + field + "'");
            }
            return srid;
        }
        return 4326;
    }

}
