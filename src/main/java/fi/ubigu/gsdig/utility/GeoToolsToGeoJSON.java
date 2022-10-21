package fi.ubigu.gsdig.utility;

import java.util.ArrayList;
import java.util.List;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GeoToolsToGeoJSON {
    
    public static Feature toGeoJSONFeature(SimpleFeature f) {
        Feature feature = new Feature();
        feature.setId(f.getID());
        feature.setGeometry(toGeometry((Geometry) f.getDefaultGeometry()));
        Name geomName = f.getDefaultGeometryProperty().getName();
        for (Property p : f.getProperties()) {
            if (p.getName().equals(geomName)) {
                continue;
            }
            feature.setProperty(p.getName().getLocalPart(), p.getValue());
        }
        return feature;
    }

    public static GeoJsonObject toGeometry(Geometry g) {
        if (g instanceof Polygon) {
            return toPolygon((Polygon) g);
        } else if (g instanceof MultiPolygon) {
            return toMultiPolygon((MultiPolygon) g);
        }
        throw new IllegalArgumentException("Geometry type " + g.getClass().getName() + " not yet implemented");
    }

    private static org.geojson.MultiPolygon toMultiPolygon(MultiPolygon g) {
        org.geojson.MultiPolygon mp = new org.geojson.MultiPolygon();
        final int n = g.getNumGeometries();
        for (int i = 0; i < n; i++) {
            mp.add(toPolygon((Polygon) g.getGeometryN(i)));
        }
        return mp;
    }

    private static org.geojson.Polygon toPolygon(Polygon g) {
        LngLatAltFilter filter = new LngLatAltFilter();
    
        org.geojson.Polygon p = new org.geojson.Polygon();
    
        g.getExteriorRing().apply(filter);
        p.setExteriorRing(filter.list);
    
        int n = g.getNumInteriorRing();
        for (int i = 0; i < n; i++) {
            filter = new LngLatAltFilter();
            g.getInteriorRingN(i).apply(filter);
            p.addInteriorRing(filter.list);
        }
    
        return p;
    }
    
    public static String getCrsName(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return null;
        }
        try {
            Integer code = CRS.lookupEpsgCode(crs, false);
            if (code != null) {
                return "urn:ogc:def:crs:EPSG::" + code;
            }
            throw new RuntimeException("Failed to determine EPSG code");
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine EPSG code", e);
        }
    }

    private static class LngLatAltFilter implements CoordinateSequenceFilter {

        private List<LngLatAlt> list = new ArrayList<>(); 

        @Override
        public void filter(CoordinateSequence seq, int i) {
            double x = seq.getX(i);
            double y = seq.getY(i);
            list.add(new LngLatAlt(x, y));
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public boolean isGeometryChanged() {
            return false;
        }

    }

}
