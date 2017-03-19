package io.joshworks.snappy.it;

import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.client.RestClient;
import io.undertow.server.handlers.form.FormData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class MultipartTest {

    private static final String FILE_PART_NAME = "fileToUpload";
    private static final String SOME_OTHER_FIELD = "parameter";

    private static Path output;

    @BeforeClass
    public static void setup() throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        output = Files.createTempFile(Paths.get(tempDir), "testFile", "output");


        multipart("/upload", (exchange) -> {
            FormData.FormValue part = exchange.part(FILE_PART_NAME);
            if (!saveFileToTemp(part.getPath())) {
                exchange.status(500);
            }

            FormData.FormValue parameter = exchange.part(SOME_OTHER_FIELD);
            String parameterValue = parameter.getValue();

            exchange.status(200).send(parameterValue, "txt");
        });

        start();
    }

    private static boolean saveFileToTemp(Path path) {
        try {
            Files.copy(path, output, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @AfterClass
    public static void shutdown() throws IOException {
        stop();
        Files.delete(output);
    }

    @Test
    public void upload() throws Exception {
        String parameterValue = "SOME-VALUE";
        String fileContent = "YOLO"; //content from the test file

        InputStream uploadFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-input.txt");
        HttpResponse<String> response = RestClient.post("http://localhost:8080/upload")
                .header("accept", "application/json")
                .field(SOME_OTHER_FIELD, parameterValue)
                .field(FILE_PART_NAME, uploadFile, "sample-input.txt")
                .asString();

        assertEquals(200, response.getStatus());
        assertTrue(Files.exists(output));

        byte[] bytes = Files.readAllBytes(output);
        assertEquals(fileContent, new String(bytes));

    }

}
