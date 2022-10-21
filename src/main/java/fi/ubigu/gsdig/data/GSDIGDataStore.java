package fi.ubigu.gsdig.data;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import fi.ubigu.gsdig.arealdivision.ArealDivision;
import fi.ubigu.gsdig.arealdivision.ArealDivisionService;

public class GSDIGDataStore extends ContentDataStore {

    protected final ArealDivisionService service;
    protected final Principal user;
    protected final DataSource ds;
    protected final int storageSrid;
    
    private List<ArealDivision> arealDivisions; 

    public GSDIGDataStore(
            ArealDivisionService service,
            Principal user,
            DataSource ds,
            int storageSrid
    ) {
        this.service = service;
        this.user = user;
        this.ds = ds;
        this.storageSrid = storageSrid;
    }

    @Override
    protected List<Name> createTypeNames() throws IOException {
        try {
            return getArealDivisions().stream()
                    .map(ArealDivision::getUuid)
                    .map(uuid -> new NameImpl(uuid.toString()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }
    
    private List<ArealDivision> getArealDivisions() throws IOException {
        if (arealDivisions == null) {
            try {
                arealDivisions = service.findAll(user);
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException(e);
            }
        }
        return arealDivisions;
    }

    @Override
    public GSDIGFeatureSource getFeatureSource(String typeName) throws IOException {
        return (GSDIGFeatureSource) super.getFeatureSource(typeName);
    }
    
    @Override
    protected GSDIGFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        UUID uuid = UUID.fromString(entry.getTypeName());
        ArealDivision ad = getArealDivisions().stream()
                .filter(it -> it.getUuid().equals(uuid))
                .findAny()
                .orElseThrow();
        return new GSDIGFeatureSource(entry, Query.ALL, ad);
    }

}
