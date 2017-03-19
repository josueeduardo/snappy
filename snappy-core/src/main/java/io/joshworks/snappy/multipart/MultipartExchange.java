package io.joshworks.snappy.multipart;

import io.joshworks.snappy.Exchange;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;

import java.util.Deque;
import java.util.Iterator;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class MultipartExchange extends Exchange {

    private final FormData formData;

    public MultipartExchange(HttpServerExchange exchange) {
        super(exchange);
        this.formData = exchange.getAttachment(FormDataParser.FORM_DATA);
    }

    public FormData.FormValue part(String partName) {
        return formData.getFirst(partName);
    }

    public Deque<FormData.FormValue> parts(String partName) {
        return formData.get(partName);
    }

    public Iterator<String> partNames() {
        return formData.iterator();
    }
}
