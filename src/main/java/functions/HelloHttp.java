package functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.IOException;
import java.util.logging.Logger;

public class HelloHttp implements HttpFunction {
    private static final Logger LOGGER = Logger.getLogger(HelloHttp.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String NAME_FIELD = "name";
    private static final String DEFAULT_NAME_VALUE = "world";

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String name = request.getFirstQueryParameter(NAME_FIELD).orElse(DEFAULT_NAME_VALUE);

        JsonNode jsonNode = objectMapper.readTree(request.getReader());
        if (jsonNode != null && jsonNode.has(NAME_FIELD)) {
            name = jsonNode.get(NAME_FIELD).asText();
        }

        ObjectNode responseJson = objectMapper.createObjectNode()
                .put("received", name)
                .put("payload", String.format("Hello %s", name));

        var writer = response.getWriter();
        response.setContentType("application/json");
        writer.write(objectMapper.writeValueAsString(responseJson));
    }
}