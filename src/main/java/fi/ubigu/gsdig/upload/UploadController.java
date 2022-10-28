package fi.ubigu.gsdig.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.ubigu.gsdig.arealdivision.ArealDivision;
import fi.ubigu.gsdig.arealdivision.ArealDivisionService;
import fi.ubigu.gsdig.arealdivision.AttributeInfo;
import fi.ubigu.gsdig.data.DataRepository;
import fi.ubigu.gsdig.data.GSDIGFeatureReader;
import fi.ubigu.gsdig.joins.JobRepository;
import fi.ubigu.gsdig.joins.JoinJob;
import fi.ubigu.gsdig.joins.JoinRequest;
import fi.ubigu.gsdig.oapif.client.HttpClient;
import fi.ubigu.gsdig.oapif.client.OAPIFDataStore;
import fi.ubigu.gsdig.permission.User;
import fi.ubigu.gsdig.unitdata.SensitivitySetting;
import fi.ubigu.gsdig.unitdata.UnitDataService;
import fi.ubigu.gsdig.unitdata.UnitDataset;
import fi.ubigu.gsdig.upload.format.UploadFormat;
import fi.ubigu.gsdig.utility.FileSafety;
import fi.ubigu.gsdig.utility.Unzip;

@RestController
@RequestMapping("/uploads")
public class UploadController {

    private static final Logger LOG = LoggerFactory.getLogger(GSDIGFeatureReader.class);

    @Autowired
    private List<UploadFormat> uploadFormats;

    @Autowired
    private UploadRepository repository;

    @Autowired
    private DataRepository dataRepository; 

    @Autowired
    private ArealDivisionService arealDivisionService;

    @Autowired
    private UnitDataService unitDataService;

    @Autowired
    private JobRepository jobRepository; 

    @Autowired
    private ObjectMapper om;

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Value("${upload.max-file-size-url}")
    private long maxFileSizeURL;

    @GetMapping
    public List<UploadInfo> findAll(Principal principal) throws Exception {
        UUID userId = User.getUserId(principal);
        return repository.findAll(userId);
    }

    @GetMapping("/{uuid}")
    public Optional<UploadInfo> findById(
            @PathVariable UUID uuid,
            Principal principal) throws Exception {
        UUID userId = User.getUserId(principal);
        return repository.findByUuid(uuid, userId);
    }

    @PostMapping
    public List<UploadInfo> upload(
            @RequestParam("file") MultipartFile in,
            Principal principal) throws Exception {
        UUID userId = User.getUserId(principal);
        return saveAsFileAndImport(userId, in.getOriginalFilename(), in::transferTo);
    }

    @PostMapping("/url")
    public List<UploadInfo> uploadFromURL(
            @RequestBody ImportFromURL importFromURL,
            Principal principal) throws Exception {
        UUID userId = User.getUserId(principal);
        return importOAPIFCollection(userId, importFromURL)
                .orElseGet(() -> saveAsFileAndImport(userId, getFileNameFromURL(importFromURL.getUrl()), file -> downloadURLtoFile(importFromURL, file)));
    }
    
    private String getFileNameFromURL(String url) {
        int j = url.indexOf('?');
        if (j < 0) {
            int i = url.lastIndexOf('/');
            return url.substring(i + 1);
        }
        int i = url.lastIndexOf('/', j - 1);
        return url.substring(i + 1, j);
    }

    private Optional<List<UploadInfo>> importOAPIFCollection(UUID userId, ImportFromURL importFromURL) {
        try {
            HttpClient http = new HttpClient(om, importFromURL.getUsername(), importFromURL.getPassword());

            // Expect inputted url to be of format:
            // <root>/collections/{collectionId}
            // <root>/collections/{collectionId}/
            
            String url = importFromURL.getUrl();
            int i, j;

            // Ignore query part
            i = url.indexOf('?');
            if (i > 0) {
                url = url.substring(0, i);
            }
            
            i = url.indexOf("/collections/");
            j = i + "/collections/".length();
            if (i < 0 || j == url.length()) {
                return Optional.empty();
            }
            String root = url.substring(0, i);
            i = j;
            
            // This doesn't work if {collectionId} contains slashes, but I'm not sure that's even valid
            j = url.indexOf('/', i);
            String typeName;
            if (j < 0) {
                // /collections/{collectionId}
                typeName = url.substring(i);
            } else if (j == url.length() - 1) {
                // /collections/{collectionId}/
                typeName = url.substring(i, j);
            } else {
                // /collections/{collectionId}/<extra> -> not link to CollectionInfo
                return Optional.empty();
            }

            DataStore store = new OAPIFDataStore(http, root, typeName);
            SimpleFeatureSource src = store.getFeatureSource(typeName);
            SimpleFeatureCollection sfc = src.getFeatures();

            List<UploadInfo> out = new ArrayList<>(1);
            trySaving(typeName, sfc, userId, out);

            return Optional.of(out);
        } catch (Exception ignore) {
            return Optional.empty();
        }
    }

    private List<UploadInfo> saveAsFileAndImport(UUID userId, String originalFileName, FileTransfer fileWriter) {
        UUID uuid = UUID.randomUUID();
        String uuidUnderscored = uuid.toString().replace('-', '_');

        File dir = new File(uploadDirectory, uuidUnderscored);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            String filename = originalFileName != null ? originalFileName : uuidUnderscored + ".tmp";
            File file = FileSafety.newFile(dir, filename);
            fileWriter.transferTo(file);
            file = Unzip.unzipIfZipFile(file, new File(dir, uuidUnderscored));
            List<UploadInfo> info = new ArrayList<>();
            for (UploadFormat uploadFormat : uploadFormats) {
                if (uploadFormat.read(file, (typeName, source) -> trySaving(typeName, source, userId, info))) {
                    return info;
                }
            }

            // Invalid format
            throw new IllegalArgumentException();
        } catch (RuntimeException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            FileSystemUtils.deleteRecursively(dir);
        }
    }

    private void downloadURLtoFile(ImportFromURL importFromURL, File file) throws IOException {
        URL url = new URL(importFromURL.getUrl());
        String user = importFromURL.getUsername();
        String pass = importFromURL.getPassword();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String accept = uploadFormats.stream()
                .map(UploadFormat::getContentType)
                .collect(Collectors.joining(", "));
        conn.setRequestProperty("Accept", accept);
        conn.addRequestProperty("Accept-Encoding", "gzip");

        String auth = HttpClient.getBasicAuthHeader(user, pass);
        if (auth != null) {
            conn.setRequestProperty("Authorization", auth);
        }

        int sc = conn.getResponseCode();
        if (sc != 200) {
            throw new IllegalArgumentException("Unexpected status code, expected 200 received " + sc);
        }

        long len = conn.getContentLengthLong();
        if (len > 0 && len > maxFileSizeURL) {
            throw new IllegalArgumentException(
                    "Content-Length is too large, maximum is: " + maxFileSizeURL + ". Received: " + len);
        }
        
        try (InputStream in = conn.getInputStream();
                InputStream inner = "gzip".equals(conn.getContentEncoding()) ? new GZIPInputStream(in, 4096) : in;
                OutputStream out = new FileOutputStream(file)) {
            inner.transferTo(out);    
        }
    }

    private void trySaving(String typeName, SimpleFeatureCollection collection, UUID userId, List<UploadInfo> out) {
        if (collection.getSchema().getGeometryDescriptor() == null) {
            throw new RuntimeException("No geometry found");
        }
        try {
            UploadInfo info = dataRepository.create(collection).withTypeName(typeName);
            repository.create(info, userId);
            out.add(info);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{uuid}/import")
    public UUID importCollection(
            @PathVariable UUID uuid,
            @RequestBody ImportCollection importRequest,
            Principal principal) throws Exception {
        UUID userId = User.getUserId(principal);

        UploadInfo uploaded = repository.findByUuid(uuid, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));

        double[] extent = uploaded.getExtent();
        Map<String, Class<?>> attributeBindings = uploaded.getAttributes();
        
        if ("arealdivision".equals(importRequest.getType())) {
            Map<String, AttributeInfo> attributes = new LinkedHashMap<>();
            attributeBindings.forEach((name, binding) -> attributes.put(name, new AttributeInfo(name, binding)));

            ArealDivision ad = new ArealDivision(uuid, userId, importRequest.getTitle(), importRequest.getDescription(), importRequest.getOrganization(), importRequest.isPublicity(), extent, attributes);
            arealDivisionService.create(ad);
            repository.delete(uuid, userId);

            return uuid;
        } else if ("unitdata".equals(importRequest.getType())) {
            Map<String, AttributeInfo> attributes = new LinkedHashMap<>();
            List<String> columnsToDrop = new ArrayList<>();
            for (Map.Entry<String, Class<?>> kvp : attributeBindings.entrySet()) {
                String name = kvp.getKey();
                Class<?> clazz = kvp.getValue();
                if (isNumeric(clazz)) {
                    attributes.put(name, new AttributeInfo(name, clazz));
                } else {
                    columnsToDrop.add(name);
                }
            }

            Object sensitivitySetting = importRequest.getTypeSpecific().get("sensitivitySetting");
            boolean remote = false;
            SensitivitySetting ss = sensitivitySetting == null
                    ? null
                    : om.convertValue(sensitivitySetting, SensitivitySetting.class);
            UnitDataset dto = new UnitDataset(uuid, userId, importRequest.getTitle(), importRequest.getDescription(), importRequest.getOrganization(), importRequest.isPublicity(), extent, attributes, remote, ss);

            unitDataService.create(dto);
            repository.delete(uuid, userId);
            for (String column : columnsToDrop) {
                dataRepository.removeAttribute(uuid, column);
            }

            return uuid;
        } else if ("job-response".equals(importRequest.getType())) {
            Map<String, AttributeInfo> attributes = new LinkedHashMap<>();
            attributeBindings.forEach((name, binding) -> attributes.put(name, new AttributeInfo(name, binding)));

            UUID jobId = om.convertValue(importRequest.getTypeSpecific().get("jobId"), UUID.class);
            JoinJob job = jobRepository.findByUuid(jobId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find job for id"));
            JoinRequest request = job.getRequest();
            
            UUID createdBy = job.getCreatedBy();
            String title = request.getTitle();
            String description = request.getDescription();
            String organization = null;
            boolean publicity = false;

            ArealDivision ad = new ArealDivision(jobId, createdBy, title, description, organization, publicity, extent, attributes);

            dataRepository.rename(uuid, jobId);
            arealDivisionService.create(ad);
            repository.delete(uuid, userId);

            return jobId;
        }

        return null;
    }
    
    private boolean isNumeric(Class<?> c) {
        return Number.class.isAssignableFrom(c);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID uuid, Principal principal) throws Exception {
        UUID userId = User.getUserId(principal);
        repository.delete(uuid, userId);
    }

    @FunctionalInterface
    public interface FileTransfer {
        
        public void transferTo(File file) throws IOException;

    }

}
