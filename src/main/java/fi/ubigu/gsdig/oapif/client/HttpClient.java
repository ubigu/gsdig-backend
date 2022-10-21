package fi.ubigu.gsdig.oapif.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpClient {
    
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE_JSON = "application/json";

    private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    private static final int DEFAULT_CONN_TIMEOUT = 1000;
    private static final int DEFAULT_READ_TIMEOUT = 15000;

    private final ObjectMapper om;
    private final String authorization;
    private final int connTimeout;
    private final int readTimeout;

    public HttpClient(ObjectMapper om, String username, String password) {
        this(om, username, password, DEFAULT_CONN_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }
    
    public HttpClient(ObjectMapper om, String username, String password, int connTimeout, int readTimeout) {
        this.om = om;
        this.authorization = getBasicAuthHeader(username, password);
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
    }

    public static String getBasicAuthHeader(String user, String pass) {
        if (user == null && pass == null) {
            return null;
        }
        String auth = user + ":" + pass;
        String base64 = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + base64;
    }

    public <T> T get(String url, Class<T> clazz) throws IOException {
        Map<String, String> headers = Collections.singletonMap(HEADER_ACCEPT, CONTENT_TYPE_JSON);
        return get(url, headers, clazz);
    }
    
    public <T> T get(String url, Map<String, String> headers, Class<T> clazz) throws IOException {
        try {
            headers.putIfAbsent(HEADER_ACCEPT, CONTENT_TYPE_JSON);
        } catch (UnsupportedOperationException ignore) {
            // Immutable maps throw UnsupportedOperationException
            // e.g. Collections.singletonMap()
        }

        LOG.info(url.toString());
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(connTimeout);
        c.setReadTimeout(readTimeout);
        if (authorization != null) {
            c.setRequestProperty(HEADER_AUTHORIZATION, authorization);
        }
        headers.forEach(c::setRequestProperty);
        int sc = c.getResponseCode();
        if (sc != HttpURLConnection.HTTP_OK) {
            throw new IOException("Unexpected response code " + sc);
        }
        try (InputStream in = c.getInputStream()) {
            return om.readValue(in, clazz);
        }
    }
    
}
