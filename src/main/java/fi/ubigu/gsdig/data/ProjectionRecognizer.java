package fi.ubigu.gsdig.data;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Envelope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProjectionRecognizer {

    private final Map<Integer, Envelope> WELL_KNOWN;
    private final Map<Integer, Envelope> KNOWN;

    public ProjectionRecognizer() {
        try {
            URL url = getClass().getClassLoader().getResource("known_projections.json");
            TypeReference<Map<Integer, double[]>> typeRef = new TypeReference<Map<Integer, double[]>>() {};
            Map<Integer, double[]> map = new ObjectMapper().readValue(url, typeRef);
            KNOWN = map.entrySet()
                    .stream()
                    .collect(Collectors.toMap(e -> e.getKey(), e -> {
                        double[] bbox = e.getValue();
                        // Envelope ctor is x1 x2 y1 y1, bbox is x1 y1 x2 y2
                        return new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
                    }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        WELL_KNOWN = new LinkedHashMap<>();
        WELL_KNOWN.put(4326, new Envelope(-180, 180, -90, 90));
        WELL_KNOWN.put(-4326, new Envelope(-90, 90, -180, 180));
        WELL_KNOWN.put(3857, new Envelope(-Math.PI * 6378137, Math.PI * 6378137, -Math.PI * 6378137, Math.PI * 6378137));
    }

    public int getSRID(Envelope extent) {
        // Simple Linear search
        // if necessary build an rtree of envelopes mapping to the srid code
        for (int srid : KNOWN.keySet()) {
            if (KNOWN.get(srid).covers(extent)) {
                return srid;
            }
        }

        for (int srid : WELL_KNOWN.keySet()) {
            if (WELL_KNOWN.get(srid).covers(extent)) {
                return srid;
            }
        }
        
        return 0;
    }

}
