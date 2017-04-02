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

package io.joshworks.snappy.extras.ssr.client;

import io.joshworks.snappy.client.UrlLookup;
import io.joshworks.snappy.extras.ssr.Instance;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by Josh Gontijo on 4/2/17.
 */
public class InstanceNameUrlLookupTest {

    private static final String DUMMY_VALID_URL = "DUMMY_VALID_URL";
    private static final int DUMMY_PORT = 8080;
    private static final String DUMMY_ADDRESS = DUMMY_VALID_URL + ":" +DUMMY_PORT;

    private UrlLookup lookup;

    @Test
    public void getUrl() throws Exception {
        String protocol = "http://";
        String serviceA = "service-a";
        lookup = new InstanceNameUrlLookup(new MockServiceStore(serviceA));

        String url = lookup.getUrl(protocol + serviceA);
        assertEquals(protocol + DUMMY_ADDRESS, url);

    }

    @Test
    public void nameWithDot() throws Exception {
        String protocol = "http://";
        String serviceA = "service.a.b";
        lookup = new InstanceNameUrlLookup(new MockServiceStore(serviceA));

        String url = lookup.getUrl(protocol + serviceA);
        assertEquals(protocol + DUMMY_ADDRESS, url);
    }

    @Test
    public void noProtocol() throws Exception {
        String serviceA = "service-a";
        lookup = new InstanceNameUrlLookup(new MockServiceStore(serviceA));

        String url = lookup.getUrl(serviceA);
        assertEquals(serviceA, url);

    }

    @Test
    public void path() throws Exception {
        String protocol = "http://";
        String serviceA = "service-name";
        String resourcePath = "/a";

        lookup = new InstanceNameUrlLookup(new MockServiceStore(serviceA));

        String url = lookup.getUrl(protocol + serviceA + resourcePath);
        assertEquals(protocol + DUMMY_ADDRESS + resourcePath, url);
    }

    @Test
    public void multiplePath() throws Exception {
        String protocol = "http://";
        String serviceA = "service-name";
        String resourcePath = "/a/b/c";

        lookup = new InstanceNameUrlLookup(new MockServiceStore(serviceA));

        String url = lookup.getUrl(protocol + serviceA + resourcePath);
        assertEquals(protocol + DUMMY_ADDRESS + resourcePath, url);
    }

    @Test
    public void emptPath() throws Exception {
        String protocol = "http://";
        String serviceA = "service-name";
        String resourcePath = "/";

        lookup = new InstanceNameUrlLookup(new MockServiceStore(serviceA));

        String url = lookup.getUrl(protocol + serviceA + resourcePath);
        assertEquals(protocol + DUMMY_ADDRESS + resourcePath, url);
    }


    public static class MockServiceStore extends ServiceStore {


        private Set<String> availableNames = new HashSet<>();

        public MockServiceStore(String... names) {
            this.availableNames.addAll(Arrays.asList(names));
        }

        @Override
        public Instance get(String serviceName) {
            if(availableNames.contains(serviceName)) {
                Instance instance = new Instance();
                instance.setHost(DUMMY_VALID_URL);
                instance.setPort(DUMMY_PORT);
                return instance;
            }
            return null;
        }
    }

}