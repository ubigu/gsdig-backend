package fi.ubigu.gsdig.oapif.client;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import fi.ubigu.gsdig.oapi.model.ConformanceClasses;
import fi.ubigu.gsdig.oapif.FeatureCollectionResponse;
import fi.ubigu.gsdig.oapif.FeatureResponse;
import fi.ubigu.gsdig.oapif.model.CollectionInfo;
import fi.ubigu.gsdig.oapif.model.CollectionsInfo;
import fi.ubigu.gsdig.oapif.model.LandingPage;
import fi.ubigu.gsdig.utility.HttpHelper;

public class FeaturesClient {

    public static final String CONTENT_TYPE_GEOJSON = "application/geo+json";

    public static LandingPage getLandingPage(HttpClient http, String root) throws IOException {
        return http.get(root, LandingPage.class);
    }

    public static CollectionsInfo getCollections(HttpClient http, String root) throws IOException {
        String url = root + "/collections";
        return http.get(url, CollectionsInfo.class);
    }

    public static CollectionInfo getCollectionInfo(HttpClient http, String root, String collectionId) throws IOException {
        String url = root + "/collections/" + collectionId;
        return http.get(url, CollectionInfo.class);
    }

    public static ConformanceClasses getConformance(HttpClient http, String root) throws IOException {
        String url = root + "/conformance";
        return http.get(url, ConformanceClasses.class);
    }

    public static FeatureCollectionResponse getFeatures(HttpClient http, String url) throws IOException {
        Map<String, String> headers = Collections.singletonMap("Accept", CONTENT_TYPE_GEOJSON);
        return http.get(url, headers, FeatureCollectionResponse.class);
    }

    public static FeatureResponse getFeature(HttpClient http, String url) throws IOException {
        Map<String, String> headers = Collections.singletonMap("Accept", CONTENT_TYPE_GEOJSON);
        return http.get(url, headers, FeatureResponse.class);
    }

    public static String getItemsURL(String root, String collectionId, Integer limit, String crs) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        if (limit != null) {
            queryParams.put("limit", Integer.toString(limit));
        }
        if (crs != null) {
            queryParams.put("crs", crs);
        }
        String items = root + "/collections/" + collectionId + "/items";
        return HttpHelper.constructUrl(items, queryParams);
    }

    public static String getNextURL(FeatureCollectionResponse response) {
        return response.getLinks().stream()
                .filter(link -> "next".equals(link.getRel()))
                .findAny()
                .map(link -> link.getHref())
                .orElse(null);
    }

}
