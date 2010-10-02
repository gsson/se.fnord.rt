package se.fnord.rt.client.testutil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.CharBuffer;


public class Utils {
    private static final String DATA_PATH = "data";
    private Utils() {}

    public static String getFile(final URL f) {
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(f.openStream());
            try {
                final StringBuilder fileData = new StringBuilder();
                final CharBuffer allocate = CharBuffer.allocate(1024);

                while(reader.read(allocate) != -1){
                    allocate.flip();
                    fileData.append(allocate);
                    allocate.clear();
                }

                return fileData.toString();
            }
            finally {
                reader.close();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
