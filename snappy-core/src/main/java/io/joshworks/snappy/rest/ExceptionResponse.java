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

package io.joshworks.snappy.rest;

import java.io.Serializable;

/**
 * Created by Josh Gontijo on 3/16/17.
 */
public class ExceptionResponse implements Serializable {

    private final String id;
    private final int status;
    private final String message;

    public ExceptionResponse(String id, int status, String message) {
        this.status = status;
        this.message = message;
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "id='" + id + "' " + "status=" + status + ", message='" + message + '\'';
    }
}
