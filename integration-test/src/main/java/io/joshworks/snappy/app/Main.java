package io.joshworks.snappy.app;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Deque;


/**
 * Created by josh on 3/10/17.
 */
public class Main {

    public static void main(String[] args) {


        HttpHandler next = exchange -> {
            InputStream inputStream = exchange.getInputStream();
            FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
            formData.forEach(partName -> {
                Deque<FormData.FormValue> formValues = formData.get(partName);
                FormData.FormValue formValue = formValues.getFirst();
                String fileName = formValue.getFileName();
                boolean isFile = formValue.isFile();
                String value = "";
                if(!isFile) {
                    value = formValue.getValue();
                }
                Path path = formValue.getPath();

                System.out.println("Yolo");


            });
        };

        EagerFormParsingHandler formHandler = new EagerFormParsingHandler();
        formHandler.setNext(next);

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(formHandler).build();
        server.start();
    }
}
