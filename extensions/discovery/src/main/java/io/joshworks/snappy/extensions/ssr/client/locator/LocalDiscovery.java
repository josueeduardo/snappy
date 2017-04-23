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

package io.joshworks.snappy.extensions.ssr.client.locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static io.joshworks.snappy.extensions.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josue on 26/08/2016.
 */
public class LocalDiscovery implements Discovery {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);

    public LocalDiscovery() {

    }


    @Override
    public HostInfo resolveHost() {
        InetAddress resolved = findHost();
        return new HostInfo(resolved.getHostAddress(), resolved.getHostName());
    }

    private InetAddress findHost() {
        InetAddress candidateAddress = null;
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> inetAddresses = nic.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String nicName = nic.getName();

                    logger.info("NIC: {} - Address: {} - Hostname: {} - IPV4: {}", nicName, inetAddress, inetAddress.getHostName(), (inetAddress instanceof Inet4Address));

                    if (nicName.startsWith("eth0") || nicName.startsWith("en0")) {
                        return inetAddress;
                    }
                    //use IPV4 only
                    if ((nicName.endsWith("0") || candidateAddress == null) && inetAddress instanceof Inet4Address) {
                        candidateAddress = inetAddress;
                    }
                }
            }

            if (candidateAddress == null) {
                logger.info("Could not find nay suitable address, trying to use 'localhost'");
                candidateAddress = InetAddress.getLocalHost();
            }
            logger.info("Using client host {}", candidateAddress);
            return candidateAddress;
        } catch (Exception e) {
            throw new RuntimeException("Cannot resolve local network address", e);
        }
    }
}