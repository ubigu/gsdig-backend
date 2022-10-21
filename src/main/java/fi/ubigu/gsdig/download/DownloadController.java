package fi.ubigu.gsdig.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fi.ubigu.gsdig.arealdivision.ArealDivisionService;
import fi.ubigu.gsdig.data.GSDIGDataStore;
import fi.ubigu.gsdig.download.export.FileExporter;
import fi.ubigu.gsdig.permission.User;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/download")
public class DownloadController {

    @Value("${endpoint}/download")
    private String root;

    @Autowired
    private ArealDivisionService arealDivisionService;
    
    @Autowired
    private DownloadRepository downloadRepository;

    @Autowired
    private Map<String, FileExporter> downloadFormats;

    @Autowired
    private DataSource ds;

    @Value("${srid.storage}")
    private int storageSrid;

    @Value("${download.directory}")
    private String downloadDirectory;

    @GetMapping
    @Operation(tags = "File download")
    public ResponseEntity<Void> getDownloadToken(
            @RequestParam UUID collectionId,
            @RequestParam String format,
            Principal principal) throws Exception {
        FileExporter exporter = downloadFormats.get(format);
        // Throw if not found
        UUID uuid = getDownloadToken(collectionId, principal, format, exporter);
        URI resource = new URI(root + "/" + uuid);
        return ResponseEntity
                .created(resource)
                .build();
    }

    private UUID getDownloadToken(UUID collectionId, Principal principal, String format, FileExporter exporter) throws Exception {
        GSDIGDataStore store = new GSDIGDataStore(arealDivisionService, principal, ds, storageSrid);
        SimpleFeatureSource source = store.getFeatureSource(collectionId.toString());
        SimpleFeatureCollection fc = source.getFeatures();

        UUID uuid = UUID.randomUUID();
        File dir = new File(downloadDirectory, uuid.toString());
        dir.mkdirs();
        File file = exporter.export(dir, fc);

        store.dispose();

        String path = file.getAbsolutePath();
        String contentType = exporter.getContentType();
        String filename = source.getName().getLocalPart() + exporter.getFileExtension();
        long length = file.length();
        DownloadFile dFile = new DownloadFile(path, contentType, filename, length);
        UUID userId = User.getUserId(principal);

        downloadRepository.store(uuid, collectionId, userId, format, dFile);

        return uuid;
    }

    @GetMapping(value = "/{token}")
    @Operation(tags = "File download")
    public void getFileDownload(
            @PathVariable UUID token,
            HttpServletResponse response) throws Exception {
        DownloadFile downloadFile = downloadRepository.getByUuid(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource"));

        File file = new File(downloadFile.getPath());
        if (!file.canRead()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file!");
        }

        response.setContentType(downloadFile.getContentType());
        response.setContentLengthLong(downloadFile.getLength());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFile.getFilename() + "\"");

        try (InputStream in = new FileInputStream(file);
                OutputStream out = response.getOutputStream()) {
            IOUtils.copyLarge(in, out);
        }

        downloadRepository.delete(token);
        file.delete();
    }
    
}
