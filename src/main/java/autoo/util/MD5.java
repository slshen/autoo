package autoo.util;

import java.security.MessageDigest;
import java.util.Formatter;

public class MD5 {
	public static byte[] hashToBytes(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(text.getBytes("UTF-8"));
		} catch (Exception e) {
			throw Rethrow.Runtime.wrap("cannot calculate hash", e);
		}
	}

	public static String hash(String text) {
		byte[] bs = hashToBytes(text);
		StringBuffer buffer = new StringBuffer(2 * bs.length);
		try (Formatter format = new Formatter(buffer)) {
			for (int i = 0; i < bs.length; i++) {
				format.format("%02x", bs[i] & 0xff);
			}
			return buffer.toString();
		}
	}
}
