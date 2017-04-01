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

package io.joshworks.snappy.client.sse;

public class EventData {
    public final String data;
    public final String lastEventId;
    public final String origin;
    public final String event;

    EventData(String data, String lastEventId, String origin, String event) {
        this.data = data;
        this.lastEventId = lastEventId;
        this.origin = origin;
        this.event = event;
    }

    EventData(String data) {
        this(data, null, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventData eventData = (EventData) o;

        if (data != null ? !data.equals(eventData.data) : eventData.data != null) return false;
        if (lastEventId != null ? !lastEventId.equals(eventData.lastEventId) : eventData.lastEventId != null)
            return false;
        return origin != null ? origin.equals(eventData.origin) : eventData.origin == null;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (lastEventId != null ? lastEventId.hashCode() : 0);
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EventData{" +
                "data='" + data + '\'' +
                ", lastEventId='" + lastEventId + '\'' +
                ", origin='" + origin + '\'' +
                '}';
    }
}
