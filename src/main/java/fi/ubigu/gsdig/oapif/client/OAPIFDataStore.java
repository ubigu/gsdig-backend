package fi.ubigu.gsdig.oapif.client;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import fi.ubigu.gsdig.oapif.CollectionInfo;

public class OAPIFDataStore extends ContentDataStore {

    protected final HttpClient http;
    protected final String root;

    private final String collectionId;
    
    private List<CollectionInfo> collectionInfoCache;

    public OAPIFDataStore(HttpClient client, String root) {
        this(client, root, null);
    }
    
    public OAPIFDataStore(HttpClient client, String root, String collectionId) {
        this.http = client;
        this.root = root;
        this.collectionId = collectionId;
    }

    @Override
    protected List<Name> createTypeNames() throws IOException {
        return getCollectionsInfo().stream()
            .map(collectionInfo -> new NameImpl(collectionInfo.getId()))
            .collect(Collectors.toList());
    }

    @Override
    protected OAPIFFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new OAPIFFeatureSource(entry);
    }
    
    private List<CollectionInfo> getCollectionsInfo() throws IOException {
        if (collectionInfoCache == null) {
            if (collectionId != null) {
                CollectionInfo info = FeaturesClient.getCollectionInfo(http, root, collectionId);
                collectionInfoCache = Collections.singletonList(info);
            } else {
                collectionInfoCache = FeaturesClient.getCollections(http, root).getCollections();
            }    
        }
        return collectionInfoCache;
    }

    public CollectionInfo getCollection(String collectionId) throws IOException {
        return getCollectionsInfo().stream()
                .filter(it -> collectionId.equals(it.getId()))
                .findAny()
                .orElse(null);
    }
    
}
