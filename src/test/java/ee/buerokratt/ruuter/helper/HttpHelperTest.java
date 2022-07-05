package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.domain.steps.http.HttpQueryArgs;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class HttpHelperTest {

    @Mock
    private MappingHelper mappingHelper;

    @Test
    void makeHttpPostRequest_shouldThrowErrorWhenUrlSyntaxError() {
        HttpHelper httpHelper = new HttpHelper(mappingHelper);
        HttpQueryArgs args = new HttpQueryArgs() {{
            setHeaders(new HashMap<>());
            setUrl("http://localhost:randomPort/endpoint");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
        }};
        HashMap<String, Object> body = args.getBody();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> httpHelper.makeHttpPostRequest(args, body));
        assertEquals(exception.getMessage(), "unsupported URI %s".formatted(args.getUrl()));
    }

    @Test
    void makeHttpGetRequest_shouldThrowErrorWhenUrlSyntaxError() {
        HttpHelper httpHelper = new HttpHelper(mappingHelper);
        HttpQueryArgs args = new HttpQueryArgs() {{
            setHeaders(new HashMap<>());
            setUrl("http://localhost:randomPort/endpoint");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
        }};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> httpHelper.makeHttpGetRequest(args));
        assertEquals(exception.getMessage(), "unsupported URI %s".formatted(args.getUrl()));
    }

}
