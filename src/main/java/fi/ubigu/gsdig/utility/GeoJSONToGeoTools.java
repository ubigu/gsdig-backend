package fi.ubigu.gsdig.utility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.GeometryCollection;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;
import org.geojson.MultiPoint;
import org.geojson.MultiPolygon;
import org.geojson.Point;
import org.geojson.Polygon;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GeoJSONToGeoTools {
    
    private static final String GEOMETRY = "geometry";

    public static SimpleFeatureType buildSchema(Iterable<Feature> features, CoordinateReferenceSystem crs, String name) {
        Map<String, Class<?>> bindings = buildSchema(features.iterator(), -1);
        
        SimpleFeatureTypeBuilder schemaBuilder = new SimpleFeatureTypeBuilder();

        schemaBuilder.setName(name);
        
        Class<?> geometryClass = bindings.remove(GEOMETRY);
        if (geometryClass != null) {
            schemaBuilder.add(GEOMETRY, geometryClass, crs);
            schemaBuilder.setDefaultGeometry(GEOMETRY);
        }
        
        bindings.forEach(schemaBuilder::add);

        return schemaBuilder.buildFeatureType();
    }
    
    public static Map<String, Class<?>> buildSchema(Iterator<Feature> features, int limit) {
        Class<? extends Geometry> currentGeometryClass = null;
        HashMap<String, Class<?>> bindings = new LinkedHashMap<>();

        for (int i = 0; i != limit && features.hasNext(); i++) {
            Feature feature = features.next();
            
            Class<? extends Geometry> geometryClass = getGeometryClass(feature.getGeometry());
            currentGeometryClass = getCompatibleGeometryClass(currentGeometryClass, geometryClass);

            feature.getProperties().forEach((k, v) -> {
                if (v != null) {
                    bindings.compute(k, (key, curr) -> getCompatibleClass(curr, v.getClass()));
                }
            });
        }
        
        bindings.put(GEOMETRY, currentGeometryClass);
        return bindings;
    }
    
    public static CoordinateReferenceSystem getCRS(Crs crs) {
        if (crs == null) {
            return DefaultGeographicCRS.WGS84;
        }
        try {
            String name = crs.getProperties().get("name").toString();
            return CRS.decode(name.toString(), true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<? extends Geometry> getGeometryClass(GeoJsonObject geometry) {
        if (geometry == null) {
            return null;
        } else if (geometry instanceof Point) {
            return org.locationtech.jts.geom.Point.class;
        } else if (geometry instanceof LineString) {
            return org.locationtech.jts.geom.LineString.class;
        } else if (geometry instanceof Polygon) {
            return org.locationtech.jts.geom.Polygon.class;
        } else if (geometry instanceof MultiPoint) {
            return org.locationtech.jts.geom.MultiPoint.class;
        } else if (geometry instanceof MultiLineString) {
            return org.locationtech.jts.geom.MultiLineString.class;
        } else if (geometry instanceof MultiPolygon) {
            return org.locationtech.jts.geom.MultiPolygon.class;
        } else if (geometry instanceof GeometryCollection) {
            return org.locationtech.jts.geom.GeometryCollection.class;
        } else {
            throw new IllegalArgumentException("Invalid geometry type");
        }
    }

    private static Class<? extends Geometry> getCompatibleGeometryClass(
            Class<? extends Geometry> curr,
            Class<? extends Geometry> next) {
        if (curr == null) {
            return next;
        } else if (curr == next || curr == Geometry.class || next == null) {
            return curr;
        } else if ((curr == org.locationtech.jts.geom.Point.class && next == org.locationtech.jts.geom.MultiPoint.class)
                || (next == org.locationtech.jts.geom.Point.class && curr == org.locationtech.jts.geom.MultiPoint.class)) {
            return org.locationtech.jts.geom.MultiPoint.class;
        } else if ((curr == org.locationtech.jts.geom.LineString.class && next == org.locationtech.jts.geom.MultiLineString.class)
                || (next == org.locationtech.jts.geom.LineString.class && curr == org.locationtech.jts.geom.MultiLineString.class)) {
            return org.locationtech.jts.geom.MultiLineString.class;
        } else if ((curr == org.locationtech.jts.geom.Polygon.class && next == org.locationtech.jts.geom.MultiPolygon.class)
                || (next == org.locationtech.jts.geom.Polygon.class && curr == org.locationtech.jts.geom.MultiPolygon.class)) {
            return org.locationtech.jts.geom.MultiPolygon.class;
        } else {
            return Geometry.class;
        }
    }

    private static Class<?> getCompatibleClass(Class<?> curr, Class<?> next) {
        if (curr == next || curr == String.class) {
            return curr;
        } else if (curr == null || next == String.class) {
            return next;
        } if (curr.isPrimitive() && next.isPrimitive() && Number.class.isAssignableFrom(curr) && Number.class.isAssignableFrom(next)) {
            if (curr == Double.class || next == Double.class || curr == Float.class || next == Float.class ) {
                return Double.class;
            } else {
                // Both are integer types, return largest
                if (curr == Long.class || next == Long.class) {
                    return Long.class;
                } else if (curr == Integer.class || next == Integer.class) {
                    return Integer.class;
                } else {
                    // Don't bother checking if one is Short and other is Byte as both can't be Byte (curr == next is already checked)
                    return Short.class;
                }
            }
        } else {
            return String.class;
        }
    }

    public static SimpleFeature toSimpleFeature(GeometryFactory gf, SimpleFeatureBuilder builder, Feature feature) {
        builder.reset();
        for (AttributeDescriptor attr : builder.getFeatureType().getAttributeDescriptors()) {
            String name = attr.getLocalName();
            Object value = GEOMETRY.equals(name)
                    ? toGeometry(gf, feature.getGeometry())
                    : feature.getProperty(name);
            builder.set(name, value);
        }
        return builder.buildFeature(feature.getId());
    }

    public static Geometry toGeometry(GeometryFactory gf, GeoJsonObject geometry) {
        if (geometry == null) {
            return null;
        } else if (geometry instanceof Point) {
            Point point = (Point) geometry;
            return gf.createPoint(toCoordinate(point.getCoordinates()));
        } else if (geometry instanceof LineString) {
            LineString linestring = (LineString) geometry;
            return gf.createLineString(toCoordinateArr(linestring.getCoordinates()));
        } else if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            return gf.createPolygon(
                    toRing(gf, polygon.getExteriorRing()),
                    toRings(gf, polygon.getInteriorRings())
                    );
        } else if (geometry instanceof MultiPoint) {
            MultiPoint mpoint = (MultiPoint) geometry;
            return gf.createMultiPointFromCoords(toCoordinateArr(mpoint.getCoordinates()));
        } else if (geometry instanceof MultiLineString) {
            MultiLineString mlinestring = (MultiLineString) geometry;
            return gf.createMultiLineString(mlinestring.getCoordinates().stream()
                    .map(linestring -> gf.createLineString(toCoordinateArr(linestring)))
                    .collect(Collectors.toList())
                    .toArray(new org.locationtech.jts.geom.LineString[0]));
        } else if (geometry instanceof MultiPolygon) {
            MultiPolygon mpolygon = (MultiPolygon) geometry;
            return gf.createMultiPolygon(
                    mpolygon.getCoordinates().stream()
                    .map(polygon -> {
                        org.locationtech.jts.geom.Polygon p = gf.createPolygon(
                                toRing(gf, polygon.get(0)),
                                toRings(gf, polygon.subList(1, polygon.size()))
                                );
                        return p;
                    })
                    .collect(Collectors.toList())
                    .toArray(new org.locationtech.jts.geom.Polygon[0])
                    );
        } else if (geometry instanceof GeometryCollection) {
            GeometryCollection geometryCollection = (GeometryCollection) geometry;
            return gf.createGeometryCollection(geometryCollection.getGeometries().stream()
                    .map(child -> toGeometry(gf, child))
                    .collect(Collectors.toList())
                    .toArray(new Geometry[0]));
        } else {
            throw new IllegalArgumentException("Invalid geometry type");
        }
    }

    private static LinearRing[] toRings(GeometryFactory gf, List<List<LngLatAlt>> listList) {
        return listList.stream()
                .map(list -> toRing(gf, list))
                .collect(Collectors.toList())
                .toArray(new LinearRing[0]);
    }

    private static LinearRing toRing(GeometryFactory gf, List<LngLatAlt> list) {
        return gf.createLinearRing(toCoordinateArr(list));
    }

    private static Coordinate[] toCoordinateArr(List<LngLatAlt> list) {
        return list.stream()
                .map(GeoJSONToGeoTools::toCoordinate)
                .collect(Collectors.toList())
                .toArray(new Coordinate[0]);
    }

    private static Coordinate toCoordinate(LngLatAlt lngLatAlt) {
        Coordinate c = new Coordinate();
        c.setX(lngLatAlt.getLongitude());
        c.setY(lngLatAlt.getLatitude());
        c.setZ(lngLatAlt.getAltitude());
        return c;
    }

}
