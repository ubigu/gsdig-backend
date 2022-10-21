package fi.ubigu.gsdig.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

public class Utils {
    
    public static final TypeReference<Map<String, Object>> MAP_STRING_TO_OBJECT = new TypeReference<Map<String, Object>>() {};
    public static final TypeReference<Map<String, Class<?>>> MAP_STRING_TO_CLASS = new TypeReference<Map<String, Class<?>>>() {};

    public static void closeSilently(AutoCloseable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception ignore) {
            // Just ignore
        }
    }

    public static String toQueryString(Map<String, String> queryParams) {
        if (queryParams.isEmpty()) {
            return "";
        }
        StringBuilder queryString = new StringBuilder("?");
        queryParams.forEach((key, value) -> {
            if (value != null) {
                queryString
                    .append(key)
                    .append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                    .append('&');
            }
        });
        // Remove last '&'
        queryString.setLength(queryString.length() - 1);
        return queryString.toString();
    }

    public static byte[] readFirstNBytes(File file, int n) throws IOException {
        if (file.length() < n) {
            throw new IllegalArgumentException("Not enough bytes available");
        }
        byte[] buf = new byte[n];
        int off = 0;
        try (InputStream in = new FileInputStream(file)) {
            while (off < n) {
                int read = in.read(buf, off, n - off);
                off += read;
            }
        }
        return buf;
    }

}
