package fi.ubigu.gsdig.utility;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpHelper {
    
    public static String constructUrl(final String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }

        final String queryString = getParams(params);
        return addQueryString(url, queryString);
    }

    public static String addQueryString(String url, String queryString) {
        if (queryString == null || queryString.trim().isEmpty()) {
            return url;
        }
        if (url == null || url.isEmpty()) {
            return queryString;
        }
        final StringBuilder urlBuilder = new StringBuilder(url);
        char lastChar = urlBuilder.charAt(urlBuilder.length()-1);
        if (!url.contains("?")) {
            lastChar = '?';
            urlBuilder.append(lastChar);
        }
        else if (lastChar != '&' && lastChar != '?') {
            lastChar = '&';
            urlBuilder.append(lastChar);
        }
        return urlBuilder.append(queryString).toString();
    }
    
    public static String getParams(Map<String, String> kvps) {
        if (kvps == null || kvps.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : kvps.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (key == null || key.isEmpty()) {
                continue;
            }
            if (value == null) {
                continue;
            }
            final String keyEnc = urlEncodePayload(key);
            final String valueEnc = urlEncodePayload(value);
            if (!first) {
                sb.append('&');
            }
            sb.append(keyEnc).append('=').append(valueEnc);
            first = false;
        }
        return sb.toString();
    }
    
    public static String urlEncodePayload(String s) {
        // URLEncoder changes white space to + that only works on application/x-www-form-urlencoded-type encoding AND needs to be used in paths
        // For parameters etc we want to have it as %20 instead
        // so http://domain/my path?q=my value SHOULD be encoded as -> http://domain/my+path?q=my%20value
        return urlEncode(s).replace("+", "%20");
    }

    public static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

}
