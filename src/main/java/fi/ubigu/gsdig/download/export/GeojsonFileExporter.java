package fi.ubigu.gsdig.download.export;

import java.io.File;
import java.util.Collections;

import org.geojson.Crs;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.ubigu.gsdig.utility.GeoToolsToGeoJSON;

@Component("json")
public class GeojsonFileExporter extends BaseFileExporter implements FileExporter {

    @Autowired
    private ObjectMapper om;

    public GeojsonFileExporter() {
        super(".json", "application/geo+json");
    }

    @Override
    public File export(File dir, SimpleFeatureCollection sfc) throws Exception {
        Crs crs = new Crs();
        crs.setProperties(Collections.singletonMap("name", getCrsName(sfc)));

        File file = File.createTempFile("tmp", ".json", dir);
        try (JsonGenerator gen = om.createGenerator(file, JsonEncoding.UTF8).useDefaultPrettyPrinter()) {
            gen.writeStartObject();

            gen.writeStringField("type", "FeatureCollection");
            gen.writeObjectField("crs", crs);

            gen.writeArrayFieldStart("features");
            try (SimpleFeatureIterator it = sfc.features()) {
                while (it.hasNext()) {
                    SimpleFeature f = it.next();
                    GeometryAttribute gAttr = f.getDefaultGeometryProperty();


                    // Open feature
                    gen.writeStartObject();

                    gen.writeStringField("id", f.getID());
                    gen.writeObjectField("geometry", GeoToolsToGeoJSON.toGeometry((Geometry) gAttr.getValue()));
                    Name geomName = gAttr.getName();

                    gen.writeObjectFieldStart("properties");
                    for (Property p : f.getProperties()) {
                        if (!p.getName().equals(geomName)) {
                            gen.writeObjectField(p.getName().getLocalPart(), p.getValue());
                        }
                    }
                    // Close properties
                    gen.writeEndObject();

                    // Close feature
                    gen.writeEndObject();
                }
            }
            gen.writeEndArray();

            gen.writeEndObject();
        }
        return file;
    }

    private String getCrsName(SimpleFeatureCollection sfc) {
        return sfc.getSchema().getAttributeDescriptors().stream()
                .filter(it -> (it instanceof GeometryDescriptor))
                .findAny()
                .map(GeometryDescriptor.class::cast)
                .map(GeometryDescriptor::getCoordinateReferenceSystem)
                .map(GeoToolsToGeoJSON::getCrsName)
                .orElse(null);
    }

}
