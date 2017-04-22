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

/**
 * Created by Josh Gontijo on 3/30/17.
 */
public class RestException extends RuntimeException {

    public final int status;

    public RestException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static RestException internalServerError() {
        return new RestException(500, "Internal server error");
    }

    public static RestException notFound(String message) {
        return new RestException(404, message);
    }

    public static RestException badRequest(String message) {
        return new RestException(400, message);
    }

    public static RestException unauthorized(String message) {
        return new RestException(401, message);
    }

    public static RestException forbidden(String message) {
        return new RestException(403, message);
    }

    public static RestException conflict(String message) {
        return new RestException(409, message);
    }

    public static RestException teapot() {
        return new RestException(418, "I'm a teapot");
    }
}
