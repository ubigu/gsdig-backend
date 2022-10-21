package fi.ubigu.gsdig.data;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.geojson.Feature;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fi.ubigu.gsdig.arealdivision.ArealDivision;
import fi.ubigu.gsdig.arealdivision.ArealDivisionService;
import fi.ubigu.gsdig.arealdivision.AttributeInfo;
import fi.ubigu.gsdig.oapif.CollectionInfo;
import fi.ubigu.gsdig.oapif.CollectionsInfo;
import fi.ubigu.gsdig.oapif.ConformanceClass;
import fi.ubigu.gsdig.oapif.ConformanceClasses;
import fi.ubigu.gsdig.oapif.FeatureCollectionResponse;
import fi.ubigu.gsdig.oapif.FeatureResponse;
import fi.ubigu.gsdig.oapif.FeaturesRequestParser;
import fi.ubigu.gsdig.oapif.GetItemsRequest;
import fi.ubigu.gsdig.oapif.LandingPage;
import fi.ubigu.gsdig.oapif.Link;
import fi.ubigu.gsdig.permission.PermissionType;
import fi.ubigu.gsdig.utility.GeoToolsToGeoJSON;
import fi.ubigu.gsdig.utility.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;

@RestController
@RequestMapping("/features")
public class GSDIGFeaturesController {

    @Value("${endpoint}")
    private String endpoint;

    @Value("${endpoint}/features")
    private String root;

    @Autowired
    private ArealDivisionService service;
    
    @Autowired
    private DataRepository dataRepo;
    
    @Autowired
    private FeaturesRequestParser parser;

    @Autowired
    private DataSource ds;

    @Value("${srid.storage}")
    private int storageSrid;

    @Bean
    public GroupedOpenApi gsdigOpenApi() {
        String paths[] = { "/features/**" };
        String packagesToScan[] = { "fi.ubigu.gsdig.join" };
        return GroupedOpenApi.builder()
                .group("gsdig")
                .pathsToMatch(paths)
                .packagesToScan(packagesToScan)
                .addOpenApiCustomiser(fixServerURLAndPaths())
                .build();
    }

    private OpenApiCustomiser fixServerURLAndPaths() {
        return openApi -> {
            openApi.setServers(Arrays.asList(new Server().url(root)));
            Paths paths = openApi.getPaths();
            List<String> keys = new ArrayList<>(paths.keySet());
            for (String key : keys) {
                if (key.startsWith("/features")) {
                    String newPath = key.substring("/features".length());
                    if (newPath.isEmpty()) {
                        newPath = "/";
                    }
                    paths.put(newPath, paths.remove(key));
                }
            }
        };
    }

    @GetMapping
    @Operation(tags = "Capabilities", summary = "landing page")
    public LandingPage getLandingPage() {
        Link self = new Link("self", "application/json", "This document", root);
        Link conformance = new Link("conformance", "application/json", "OGC API conformance classes implemented by this server", root + "/conformance");
        Link data = new Link("data", "application/json", "Access the data", root + "/collections");
        Link service_desc = new Link("service-desc", "application/vnd.oai.openapi+json;version=3.0", "Definition of the API in OpenAPI 3.0", endpoint + "/openapi");

        LandingPage landingPage = new LandingPage();
        landingPage.setTitle("GSDIG Features");
        landingPage.setDescription("GSDIG Features as OGC API Features service");
        landingPage.setLinks(Arrays.asList(self, conformance, data, service_desc));
        return landingPage;
    }

    @GetMapping("/conformance")
    @Operation(tags = "Capabilities", summary = "conformance declaration")
    public ConformanceClasses getConformance() {
        List<String> conformanceURIs = Arrays.stream(ConformanceClass.values())
                .map(it -> it.url)
                .collect(Collectors.toList());

        ConformanceClasses classes = new ConformanceClasses();
        classes.setConformsTo(conformanceURIs);
        return classes;
    }

    @GetMapping("/collections")
    @Operation(tags = "Discover data collections", summary = "feature collections in the dataset")
    public CollectionsInfo getCollections(Principal principal) throws Exception {
        List<CollectionInfo> collections = service.findAll(principal).stream()
                .map(this::toCollectionInfo)
                .toList();

        String selfHref = root + "/collections";
        Link self = new Link("self", "application/json", "This document", selfHref);
        List<Link> links = Collections.singletonList(self);

        CollectionsInfo info = new CollectionsInfo();
        info.setCollections(collections);
        info.setLinks(links);
        return info;
    }

    @GetMapping("/collections/{collectionId}")
    @Operation(tags = "Discover data collections", summary = "feature collection by id")
    public CollectionInfo getCollectionById(
            @PathVariable UUID collectionId,
            Principal principal) throws Exception {
        return service.findById(collectionId, principal)
                .map(this::toCollectionInfo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
    }

    private CollectionInfo toCollectionInfo(ArealDivision ad) {
        CollectionInfo info = new CollectionInfo();
        info.setId(ad.getUuid().toString());
        info.setTitle(ad.getTitle());
        info.setDescription(ad.getDescription());
        info.setCrs(parser.getCrs());
        info.setStorageCrs(parser.getStorageCrs());
        return info;
    }

    @GetMapping(value = "/collections/{collectionId}/items", produces = "application/geo+json")
    @Operation(tags = "Access data", responses=@ApiResponse(content = @Content(mediaType = "application/geo+json", schema = @Schema(ref = "http://schemas.opengis.net/ogcapi/features/part1/1.0/openapi/schemas/featureCollectionGeoJSON.yaml"))))
    public FeatureCollectionResponse getItems(
            @PathVariable String collectionId,
            @RequestParam(required = false) @ArraySchema(minItems = 4, maxItems = 6, schema = @Schema(type = "number")) String bbox,
            @RequestParam(required = false) String datetime,
            @RequestParam(defaultValue = "0") @Min(0) @Valid int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10000) @Valid int limit,
            @RequestParam(required = false) String crs,
            @RequestParam(required = false, name="bbox-crs") String bboxCrs,
            Principal principal
    ) throws Exception {
        GetItemsRequest request = parser.parse(collectionId, bbox, datetime, offset, limit, crs, bboxCrs);

        Query query = new Query(collectionId);
        query.setMaxFeatures(request.getLimit());
        query.setStartIndex(request.getOffset());
        query.setCoordinateSystemReproject(CRS.decode("EPSG:" + request.getSrid(), true));

        GSDIGDataStore store = new GSDIGDataStore(service, principal, ds, storageSrid);
        
        SimpleFeatureSource source;
        try {
            source = store.getFeatureSource(collectionId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown collectionId: " + collectionId);
        }
        List<Feature> features = toList(source.getFeatures(query), GeoToolsToGeoJSON::toGeoJSONFeature);

        Integer nextOffset = null;
        if (features.size() == limit) {
            nextOffset = offset + limit;
        }

        Map<String, String> queryParams = new HashMap<>();
        if (bbox != null) {
            queryParams.put("bbox", bbox);
        }
        if (datetime != null) {
            queryParams.put("datetime", datetime);
        }
        queryParams.put("offset", "" + offset);
        queryParams.put("limit", "" + limit);
        queryParams.put("crs", crs);
        queryParams.put("bbox-crs", bboxCrs);

        String baseHref = root + "/collections/" + collectionId + "/items";
        String selfHref = baseHref + Utils.toQueryString(queryParams);

        List<Link> links = new ArrayList<>();
        links.add(new Link("self", "application/geo+json", "This document", selfHref));

        if (nextOffset != null) {
            queryParams.put("offset", "" + nextOffset);
            String nextHref = baseHref + Utils.toQueryString(queryParams);
            links.add(new Link("next", "application/geo+json", "Next page", nextHref));
        }

        FeatureCollectionResponse response = new FeatureCollectionResponse();
        response.setFeatures(features);
        response.setLinks(links);
        response.setNumberReturned(features.size());
        response.setTimeStamp(Instant.now());
        return response;
    }
    
    @PutMapping(value = "/collections/{collectionId}/items")
    public void updateItems(
            @PathVariable String collectionId,
            @RequestBody List<Feature> features,
            Principal principal
    ) throws Exception {
        if (features.isEmpty()) {
            return;
        }

        UUID arealDivisionId = UUID.fromString(collectionId);
        ArealDivision ad = service.findById(arealDivisionId, principal, PermissionType.WRITE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
        
        Map<String, AttributeInfo> schema = ad.getAttributes();
        for (Feature f : features) {
            String err = schemaValidate(schema, f.getProperties());
            if (err != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, err);
            }
        }

        dataRepo.batchUpdate(ad.getUuid(), features);
    }
    
    private String schemaValidate(Map<String, AttributeInfo> schema, Map<String, Object> properties) {
        for (String key : schema.keySet()) {
            if (!properties.containsKey(key)) {
                return "Missing property " + key;
            }
            // TODO: Check if object value can be parsed into property class binding
        }
        if (properties != null) {
            for (String key : properties.keySet()) {
                if (!schema.containsKey(key)) {
                    return "Extraneous property " + key;
                }
            }
        }
        return null;
    }

    @GetMapping(value = "/collections/{collectionId}/items/{featureId}", produces = "application/geo+json")
    @Operation(tags = "Access data", responses=@ApiResponse(content = @Content(mediaType = "application/geo+json", schema = @Schema(ref = "http://schemas.opengis.net/ogcapi/features/part1/1.0/openapi/schemas/featureGeoJSON.yaml"))))
    public FeatureResponse getItemById(
            @PathVariable String collectionId,
            @PathVariable long featureId,
            @RequestParam(required = false) String crs,
            Principal principal
    ) throws Exception {
        Query query = new Query(collectionId);
        query.setMaxFeatures(1);
        query.setStartIndex(0);
        query.setCoordinateSystem(CRS.decode("EPSG:" + parser.getSrid(crs, "crs"), true));

        GSDIGDataStore store = new GSDIGDataStore(service, principal, ds, storageSrid);
        SimpleFeatureSource source = store.getFeatureSource(collectionId);
        List<Feature> features = toList(source.getFeatures(query), GeoToolsToGeoJSON::toGeoJSONFeature);
        if (features.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
        }
        Feature feature = features.get(0);

        String collectionHref = root + "/collections/" + collectionId;
        String selfHref = collectionHref + "/items/" + featureId;

        List<Link> links = new ArrayList<>();
        links.add(new Link("self", "application/geo+json", "This document", selfHref));
        links.add(new Link("collection", "application/json", "The collection the feature belongs to", collectionHref));

        FeatureResponse response = new FeatureResponse();
        response.setGeometry(feature.getGeometry());
        response.setId(feature.getId());
        response.setCrs(feature.getCrs());
        response.setProperties(feature.getProperties());
        response.setLinks(links);
        return response;
    }

    /*
    @PostMapping(value = "/collections/{collectionId}/merge")
    @Operation(tags = "Merge features")
    public ResponseEntity<Void> merge(
            @PathVariable UUID collectionId,
            @RequestBody MergeRequest mergeRequest) throws Exception {
        
        DataAggregate da = aggregateRepository.findByUuid(collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));
        

        DataAggregateJob job = jobRepository.findByUuid(collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));

        DataAggregateRequest request = job.getRequest();
        request.setArealDivision(collectionId);
        
        jobRepository.create(request);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
    */

    /*
    @PostMapping(value = "/collections/{collectionId}/items/{featureId}/unmerge")
    @Operation(tags = "Unmerge data")
    public long[] unmerge(
            @PathVariable UUID collectionId,
            @PathVariable long featureId) throws Exception {
        DataAggregate da = aggregateRepository.findByUuid(collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));

        long[] unmergedIds = featureMerger.unmerge(da.getArealDivision(), featureId);

        service.refresh(da);

        return unmergedIds;
    }
    */

    private static final <T> List<T> toList(SimpleFeatureCollection fc, Function<SimpleFeature, T> map) {
        try (SimpleFeatureIterator it = fc.features()) {
            List<T> list = new ArrayList<>();
            while (it.hasNext()) {
                T t = map.apply(it.next());
                list.add(t);
            }
            return list;
        }
    }

}
