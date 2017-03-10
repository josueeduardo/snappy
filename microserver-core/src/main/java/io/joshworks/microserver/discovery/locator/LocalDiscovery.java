package io.joshworks.microserver.discovery.locator;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * Created by Josue on 26/08/2016.
 */
public class LocalDiscovery implements Discovery {

    private static final Logger logger = Logger.getLogger(LocalDiscovery.class.getName());

    private final InetAddress address;

    public LocalDiscovery() {
        address = initialiseHosts();
    }

    private InetAddress initialiseHosts() {
        InetAddress candidateAddress = null;
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> inetAddresses = nic.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String nicName = nic.getName();

                    logger.info("NIC: " + nicName + " -> Address: " + inetAddress + " -> Hostname: " + inetAddress.getHostName() + " -> IPV4: " + (inetAddress instanceof Inet4Address));

                    if (nicName.startsWith("eth0") || nicName.startsWith("en0")) {
                        return inetAddress;
                    }
                    //use IPV4 only
                    if ((nicName.endsWith("0") || candidateAddress == null) && inetAddress instanceof Inet4Address) {
                        candidateAddress = inetAddress;
                    }
                }
            }

            return candidateAddress;
        } catch (Exception e) {
            throw new RuntimeException("Cannot resolve local network address", e);
        }
    }

    @Override
    public String resolveHost(boolean useHostname) {
        return resolve(address, useHostname);
    }

    private String resolve(InetAddress candidate, boolean useHost) {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            InetAddress resolved = candidate == null ? localhost : candidate;
            return useHost ? resolved.getHostName() : resolved.getHostAddress();

        } catch (Exception e) {
            throw new RuntimeException("Error resolving host", e);
        }
    }


}
