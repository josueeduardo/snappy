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
import io.joshworks.restclient.http.SimpleClient;
import io.joshworks.snappy.multipart.Part;
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


        multipart("/upload", exchange -> {
            Part part = exchange.part(FILE_PART_NAME);
            if (!saveFileToTemp(part.file().path())) {
                exchange.status(500);
            }

            Part parameter = exchange.part(SOME_OTHER_FIELD);
            String parameterValue = parameter.value();

            exchange.status(200).send(parameterValue, "txt");
        });

        group("/a", () -> {
            multipart("/b", exchange -> {
                exchange.status(201);
            });
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
        HttpResponse<String> response = SimpleClient.post("http://localhost:9000/upload")
                .header("accept", "application/json")
                .field(SOME_OTHER_FIELD, parameterValue)
                .field(FILE_PART_NAME, uploadFile, "sample-input.txt")
                .asString();

        assertEquals(200, response.getStatus());
        assertTrue(Files.exists(output));

        byte[] bytes = Files.readAllBytes(output);
        assertEquals(fileContent, new String(bytes));

    }

    @Test
    public void resolveUrl() throws Exception {
        String parameterValue = "SOME-VALUE";

        InputStream uploadFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-input.txt");
        HttpResponse<String> response = SimpleClient.post("http://localhost:9000/a/b")
                .header("accept", "application/json")
                .field(SOME_OTHER_FIELD, parameterValue)
                .field(FILE_PART_NAME, uploadFile, "sample-input.txt")
                .asString();

        assertEquals(201, response.getStatus());
    }

}
