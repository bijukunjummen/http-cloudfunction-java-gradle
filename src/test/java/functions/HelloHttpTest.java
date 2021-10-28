package functions;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HelloHttpTest {
    @Mock
    private HttpRequest request;
    @Mock
    private HttpResponse response;

    private BufferedWriter writerOut;
    private StringWriter responseOut;
    private static final Gson gson = new Gson();

    @BeforeEach
    public void beforeTest() throws IOException {
        // use an empty string as the default request content
        BufferedReader reader = new BufferedReader(new StringReader(""));
        when(request.getReader()).thenReturn(reader);

        responseOut = new StringWriter();
        writerOut = new BufferedWriter(responseOut);
        when(response.getWriter()).thenReturn(writerOut);
    }

    @Test
    public void helloHttp_noParamsGet() throws IOException {
        new HelloHttp().service(request, response);

        writerOut.flush();
        assertThat(responseOut.toString()).isEqualTo("Hello world!");
    }

    @Test
    public void helloHttp_urlParamsGet() throws IOException {
        when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("Tom"));

        new HelloHttp().service(request, response);

        writerOut.flush();
        assertThat(responseOut.toString()).isEqualTo("Hello Tom!");
    }

    @Test
    public void helloHttp_bodyParamsPost() throws IOException {
        String requestJson = gson.toJson(Map.of("name", "Jane"));
        BufferedReader jsonReader = new BufferedReader(new StringReader("{'name': 'Jane'}"));

        when(request.getReader()).thenReturn(jsonReader);

        new HelloHttp().service(request, response);
        writerOut.flush();

        assertThat(responseOut.toString()).isEqualTo("Hello Jane!");
    }
}