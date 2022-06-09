package ee.buerokratt.ruuter.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StringUtils {

    private StringUtils() {
    }

    public static List<String> convertHeadersMapToList(Map<String, String> headersMap) {
        if (headersMap != null) {
            List<String> headers = new ArrayList<>();
            headersMap.forEach((k, v) -> {
                headers.add(k);
                headers.add(v);
            });
            return headers;
        }
        return Collections.emptyList();
    }

    public static String getUrlWithQuery(String url, Map<String, Object> query) {
        if (query != null) {
            return url + query.entrySet().stream()
                .map(p -> p.getKey() + "=" + p.getValue())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .map(s -> "?" + s)
                .orElse("");
        }
        return url;
    }
}
