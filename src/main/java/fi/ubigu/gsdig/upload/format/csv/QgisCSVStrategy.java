package fi.ubigu.gsdig.upload.format.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.csv.CSVFileState;
import org.geotools.data.csv.parse.CSVStrategy;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class QgisCSVStrategy extends CSVStrategy {
    
    public QgisCSVStrategy(CSVFileState csvFileState) {
        super(csvFileState);
    }

    @Override
    protected SimpleFeatureType buildFeatureType() {
        Map<String, TypeTester> result;
        String[] headers;

        try (CSVReader csvReader = csvFileState.openCSVReader()) {
            headers = csvFileState.getCSVHeaders();
            result = findTypesFromData(csvReader, headers);
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Failure reading csv file", e);
        }
        
        Map<String, Class<?>> typesFromData = new HashMap<>();
        result.forEach((key, value) -> typesFromData.put(key, value.getDetectedType()));
        
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(csvFileState.getTypeName());
        if (csvFileState.getNamespace() != null) {
            builder.setNamespaceURI(csvFileState.getNamespace());
        }
        for (String col : headers) {
            Class<?> type = typesFromData.get(col);
            if (type == Geometry.class) {
                builder.add(col, type);
                if (builder.getDefaultGeometry() == null) {
                    builder.setDefaultGeometry(col);
                }
            } else {
                builder.add(col, type);
            }
        }
        return builder.buildFeatureType();
    }
    
    private Map<String, TypeTester> findTypesFromData(CSVReader csvReader, String[] headers) throws IOException {
        Map<String, TypeTester> result = new HashMap<>();
        for (String header : headers) {
            result.put(header, new TypeTester(Arrays.asList(
                    new TypeTest(Integer.class, QgisCSVStrategy::isInt),
                    new TypeTest(Long.class, QgisCSVStrategy::isLong),
                    new TypeTest(Double.class, QgisCSVStrategy::isDouble),
                    new TypeTest(Geometry.class, QgisCSVStrategy::isGeometry),
                    new TypeTest(String.class, null)
            )));
        }
        
        try {
            // Read through the whole file in case the type changes in later rows
            String[] record;
            while ((record = csvReader.readNext()) != null) {
                int n = Math.min(record.length, headers.length);
                for (int i = 0; i < n; i++) {
                    result.get(headers[i]).test(record[i].trim());
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException(e);
        }
        
        return result;
    }
    
    @Override
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        
    }

    @Override
    public String[] encode(SimpleFeature feature) {
        List<String> csvRecord = new ArrayList<>();
        for (Property property : feature.getProperties()) {
            Object value = property.getValue();
            if (value == null) {
                csvRecord.add("");
            } else if (Geometry.class.isAssignableFrom(value.getClass())) {
                WKTWriter wkt = new WKTWriter();
                csvRecord.add(wkt.write((Geometry) value));
            } else {
                csvRecord.add(value.toString());
            }
        }
        return csvRecord.toArray(new String[csvRecord.size() - 1]);
    }

    @Override
    public SimpleFeature decode(String recordId, String[] csvRecord) {
        SimpleFeatureType featureType = getFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        
        String[] headers = csvFileState.getCSVHeaders();
        int geometryIdx = getGeometryIndex(headers, featureType.getGeometryDescriptor());

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            if (i < csvRecord.length) {
                String value = csvRecord[i].trim();
                if (i == geometryIdx) {
                    WKTReader wktReader = new WKTReader();
                    Geometry geometry;
                    try {
                        geometry = wktReader.read(value);
                    } catch (ParseException e) {
                        geometry = null;
                    }
                    builder.set(header, geometry);
                } else {
                    if (value.isEmpty() && featureType.getType(header).getBinding() != String.class) {
                        builder.set(header, null);
                    } else {
                        builder.set(header, value);
                    }
                }
            } else {
                if (csvRecord.length == 1 && csvRecord[0].isEmpty()) {
                    return null;
                }
                builder.set(header, null);
            }
        }
        return builder.buildFeature(csvFileState.getTypeName() + "-" + recordId);
    }
    
    private int getGeometryIndex(String[] headers, GeometryDescriptor geometryDesc) {
        if (geometryDesc != null) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equals(geometryDesc.getLocalName())) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private static Boolean isInt(String s) {
        try {
            if (s.isEmpty()) {
                return true;
            }
            long v = Long.parseLong(s);
            return v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE;
        } catch (Exception e) {
            return false;
        }
    }

    private static Boolean isLong(String s) {
        try {
            if (s.isEmpty()) {
                return true;
            }
            Long.parseLong(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Boolean isDouble(String s) {
        try {
            if (s.isEmpty()) {
                return true;
            }
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static final WKTReader WKT_READER = new WKTReader();
    
    private static Boolean isGeometry(String s) {
        try {
            if (s.isEmpty()) {
                return true;
            }
            WKT_READER.read(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
