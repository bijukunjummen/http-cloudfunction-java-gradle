package functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelloHttpTest {
    @Mock
    private HttpRequest request;
    @Mock
    private HttpResponse response;

    private BufferedWriter writerOut;
    private StringWriter responseOut;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void beforeEach() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(""));
        when(request.getReader()).thenReturn(reader);

        responseOut = new StringWriter();
        writerOut = new BufferedWriter(responseOut);
        when(response.getWriter()).thenReturn(writerOut);
    }

    @Test
    void helloHttpNoBodyTest() throws Exception {
        new HelloHttp().service(request, response);
        writerOut.flush();
        JSONAssert.assertEquals(
                """
                         {"received": "world", "payload": "Hello world"}
                        """,
                responseOut.toString(), true);
    }

    @Test
    void helloHttpWithParam() throws Exception {
        when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("Tom"));
        new HelloHttp().service(request, response);
        writerOut.flush();
        JSONAssert.assertEquals(
                """
                         {"received": "Tom", "payload": "Hello Tom"}
                        """,
                responseOut.toString(), true);
    }

    @Test
    void helloHttpWithBody() throws Exception {
        when(request.getReader())
                .thenReturn(new BufferedReader(
                        new StringReader(objectMapper.writeValueAsString(Map.of("name", "Tom")))));
        when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("something else"));

        new HelloHttp().service(request, response);

        writerOut.flush();
        JSONAssert.assertEquals(
                """
                         {"received": "Tom", "payload": "Hello Tom"}
                        """,
                responseOut.toString(), true);
    }
}