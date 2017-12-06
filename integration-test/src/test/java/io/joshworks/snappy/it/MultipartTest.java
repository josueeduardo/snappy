/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.joshworks.snappy.it;

import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.JsonNode;
import io.joshworks.restclient.http.Unirest;
import io.joshworks.snappy.http.multipart.Part;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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


        enableTracer();
        multipart("/upload", exchange -> {
            Part part = exchange.part(FILE_PART_NAME);
            if (!saveFileToTemp(part.file().path())) {
                exchange.status(500);
            }

            Part parameter = exchange.part(SOME_OTHER_FIELD);
            String parameterValue = parameter.value();

            exchange.status(200).send(parameterValue, "txt");
        });

        multipart("/partMime", exchange -> {
            Map<String, Object> mime = new HashMap<>();
            for (Part part : exchange.parts()) {
                mime.put(part.name(), part.type().toString());
            }

            exchange.send(mime, "json");
        });

        group("/a", () -> {
            multipart("/b", exchange -> {
                exchange.status(201);
            });
        });

        start();
    }

    @AfterClass
    public static void shutdown() throws IOException {
        stop();
        Files.delete(output);
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

    @Test
    public void upload() throws Exception {
        String fileContent = "YOLO"; //content from the test file

        InputStream uploadFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-input.txt");
        HttpResponse<String> response = Unirest.post("http://localhost:9000/upload")
                .part(FILE_PART_NAME, uploadFile, "sample-input.txt")
                .asString();

        assertEquals(200, response.getStatus());
        assertTrue(Files.exists(output));

        byte[] bytes = Files.readAllBytes(output);
        assertEquals(fileContent, new String(bytes));
    }

    @Test
    public void uploadBinaryWithText() throws Exception {
        String parameterValue = "SOME-VALUE";
        String fileContent = "YOLO"; //content from the test file

        InputStream uploadFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-input.txt");
        HttpResponse<String> response = Unirest.post("http://localhost:9000/upload")
                .part(SOME_OTHER_FIELD, parameterValue)
                .part(FILE_PART_NAME, uploadFile, "sample-input.txt")
                .asString();

        assertEquals(200, response.getStatus());
        assertTrue(Files.exists(output));
        assertEquals(parameterValue, response.getBody());//server returns the text parameter

        byte[] bytes = Files.readAllBytes(output);
        assertEquals(fileContent, new String(bytes));
    }

//    FIXME use rest-client 1.5.2
    @Test
    public void partMime() {
        InputStream uploadFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-input.txt");
        HttpResponse<JsonNode> response = Unirest.post("http://localhost:9000/partMime")
                .part("textPart", "someContent", "text/plain")
                .part("jsonPart", "{}", "application/json")
                .part("filePart", uploadFile, "sample-input.txt")
                .asJson();

        assertEquals(200, response.getStatus());
        assertNotNull(response.getBody());
        assertEquals("text/plain", response.getBody().getObject().getString("textPart"));
        assertEquals("application/json", response.getBody().getObject().getString("jsonPart"));
        assertEquals("application/octet-stream", response.getBody().getObject().getString("filePart"));
    }


    @Test
    public void resolveUrl() {
        HttpResponse<String> response = Unirest.post("http://localhost:9000/a/b")
                .header("accept", "application/json")
                .asString();

        assertEquals(201, response.getStatus());
    }


}
