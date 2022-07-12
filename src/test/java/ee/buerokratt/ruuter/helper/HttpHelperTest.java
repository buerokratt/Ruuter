package ee.buerokratt.ruuter.helper;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpHelperTest {

    @Test
    void doPost_shouldThrowErrorWhenUrlSyntaxError() {
        HttpHelper httpHelper = new HttpHelper();
        String url = "http://localhost:randomPort/endpoint";
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> query = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> httpHelper.doPost(url, body, query, headers));
    }

    @Test
    void doGet_shouldThrowErrorWhenUrlSyntaxError() {
        HttpHelper httpHelper = new HttpHelper();
        String url = "http://localhost:randomPort/endpoint";
        HashMap<String, Object> query = new HashMap<>();
        HashMap<String, String> headers = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> httpHelper.doGet(url, query, headers));
    }
}
