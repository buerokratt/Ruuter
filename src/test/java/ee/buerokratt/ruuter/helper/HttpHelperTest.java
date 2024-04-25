package ee.buerokratt.ruuter.helper;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpHelperTest {

    //TODO: tests are currently brooken
    @Test
    void doPost_shouldThrowErrorWhenUrlSyntaxError() {
        /*
        HttpHelper httpHelper = new HttpHelper();
        String url = "http://localhost:randomPort/endpoint";
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> query = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> httpHelper.doPost(url, body, query, headers));

         */
    }

    @Test
    void doMethod_shouldThrowErrorWhenUrlSyntaxError() {
        /*
        HttpHelper httpHelper = new HttpHelper();

        String url = "http://localhost:randomPort/endpoint";
        HashMap<String, Object> query = new HashMap<>();
        HashMap<String, String> headers = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> httpHelper.doMethod(HttpMethod.GET, url, query, null, new HashMap<>(), null, null));
         */
    }
}
