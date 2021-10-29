package functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class HelloHttp implements HttpFunction {
    private static final Logger logger = Logger.getLogger(HelloHttp.class.getName());

    private static final Gson gson = new Gson();

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        // Check URL parameters for "name" field
        // "world" is the default value
        String prefix = System.getenv("message");
        if (prefix == null) prefix = "Hello";
        String name = request.getFirstQueryParameter("name").orElse("world");

        // Parse JSON request and check for "name" field
        try {
            JsonElement requestParsed = gson.fromJson(request.getReader(), JsonElement.class);
            JsonObject requestJson = null;

            if (requestParsed != null && requestParsed.isJsonObject()) {
                requestJson = requestParsed.getAsJsonObject();
            }

            if (requestJson != null && requestJson.has("name")) {
                name = requestJson.get("name").getAsString();
            }
        } catch (JsonParseException e) {
            logger.severe("Error parsing JSON: " + e.getMessage());
        }

        var writer = new PrintWriter(response.getWriter());
        writer.printf("%s %s!", prefix, name);
    }
}