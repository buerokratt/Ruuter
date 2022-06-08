package ee.buerokratt.ruuter.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
public class Args<T> {

    private String url;
    private HashMap<String, T> query;
    private HashMap<String, String> headers;
    private HashMap<String, T> body;

}
