package autoo.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.logging.Logger;

public final class UUID {
    private static Logger _logger = Logging.getLogger(UUID.class);
    private UUID() {
    }
    /**
     * Returns a globally unique identifier based on the current time, the ip addresses
     * of all the network interfaces, and a secure random long from SHA1PRNG (from SecureRandom). 
     * @return the MD5 hash of the strings of all of the above.
     */
    public static String getUUID() {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append(System.currentTimeMillis());
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intf = interfaces.nextElement();
                Enumeration<InetAddress> addrs = intf.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    buffer.append(".").append(addr);
                }
            }
            try {
                SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
                buffer.append(".").append(r.nextLong());
            } catch (NoSuchAlgorithmException e) {
                _logger.warning("could not add secure random number to UUID: " + e);
            }
            return MD5.hash(buffer.toString());
        } catch (SocketException e) {
            throw new RuntimeException("could not generate UUID: " + e);
        }
    }
}
