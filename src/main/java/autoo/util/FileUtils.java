package autoo.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {
    private FileUtils() {
    }
    
    public static void recursiveDelete(File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                recursiveDelete(file);
            }
        }
        if (dir.exists() && !dir.delete()) {
            throw new IOException("could not delete " + dir);
        }
    }
    
    public static String getContents(File f) throws IOException {
    	return getContents(new FileReader(f));
    }
    
    public static String getContents(Reader in) throws IOException {
    	try {
    		StringBuilder builder = new StringBuilder();
    		CharBuffer buffer = CharBuffer.allocate(4096);
    		while (in.read(buffer) != -1) {
    			builder.append(buffer.array(), 0, buffer.position());
    			buffer.rewind();
    		}
    		return builder.toString();
    	} finally {
    		in.close();
    	}
    }
    
    /**
     * Return a file name in a directory named base . yyyyMMdd . sequence . suffix.
     * Sequence will be the smallest integer that makes the file unique. 
     */
    public static File getTimestampedFile(File dir, String base, String suffix) {
    	String timestamp = getTimestamp();
    	int n = 0;
    	File f;
    	do {
    		f = new File(dir, base + "." + timestamp + "." + n++ + "." + suffix);
    	} while (f.exists());
    	return f;
    }

    /**
     * Returns a timestamp in the form yyyyMMdd.
     */
	public static String getTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(new Date());
	}
}

