package ee.buerokratt.ruuter.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import java.util.HashMap;

@Getter
@Setter
@Service
@NoArgsConstructor
public class Args<T> {

    private String url;
    private HashMap<String, T> query;
    private HashMap<String, String> headers;
    private HashMap<String, T> body;

    @Override
    public String toString(){
        return "url: %s, query: %s, headers: %s, body: %s".formatted(url, query, headers, body);
    }
}
