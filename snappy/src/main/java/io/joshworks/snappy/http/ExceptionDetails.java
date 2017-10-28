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

package io.joshworks.snappy.http;

/**
 * Created by Josh Gontijo on 3/27/17.
 */
public class ExceptionDetails<T extends Exception> {

    public final long timestamp;
    public final T exception;
    public final int assignedStatusCode;

    public ExceptionDetails(T exception) {
        assignedStatusCode = exception instanceof HttpException ? ((HttpException) exception).status : 500;
        this.timestamp = System.currentTimeMillis();
        this.exception = exception;
    }
}