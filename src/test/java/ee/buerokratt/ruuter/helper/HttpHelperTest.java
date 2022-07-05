package ee.buerokratt.ruuter.helper;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpHelperTest {

    @Test
    void post_shouldThrowErrorWhenUrlSyntaxError() {
        HttpHelper httpHelper = new HttpHelper();
        String url = "http://localhost:randomPort/endpoint";
        HashMap<String, Object> body = new HashMap<>();
        HashMap<String, Object> query = new HashMap<>();
        HashMap<String, String> headers = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> httpHelper.post(url, body, query, headers));
    }

    @Test
    void get_shouldThrowErrorWhenUrlSyntaxError() {
        HttpHelper httpHelper = new HttpHelper();
        String url = "http://localhost:randomPort/endpoint";
        HashMap<String, Object> query = new HashMap<>();
        HashMap<String, String> headers = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> httpHelper.get(url, query, headers));
    }
}
