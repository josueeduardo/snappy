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

package io.joshworks.snappy.multipart;

import io.joshworks.snappy.Exchange;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class MultipartExchange extends Exchange {

    private final FormData formData;

    public MultipartExchange(HttpServerExchange exchange) {
        super(exchange);
        this.formData = exchange.getAttachment(FormDataParser.FORM_DATA);
    }

    public Part part(String partName) {
        return formData == null ? new Part() : new Part(formData.getFirst(partName));
    }

    public List<Part> parts(String partName) {
        List<Part> parts = new ArrayList<>();
        if (formData == null) {
            return parts;
        }
        Deque<FormData.FormValue> formValues = formData.get(partName);
        formValues.forEach(p -> parts.add(new Part(p)));
        return parts;
    }

    public List<String> partNames() {
        List<String> names = new ArrayList<>();
        if (formData == null) {
            return names;
        }
        formData.iterator().forEachRemaining(names::add);
        return names;
    }
}
