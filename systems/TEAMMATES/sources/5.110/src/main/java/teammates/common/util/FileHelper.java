package teammates.common.util;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Holds file-related functions.
 */
public final class FileHelper {

    private FileHelper() {
        // utility class
    }

    /**
     * Reads the contents of a file in the {@code resources} folder
     * as an {@link InputStream}.
     * @param file The file name, which must be in the {@code resources} folder.
     */
    public static InputStream getResourceAsStream(String file) {
        return Config.class.getClassLoader().getResourceAsStream(file);
    }

    /**
     * Reads the contents of a file in the {@code resources} folder.
     * @param file The file name, which must be in the {@code resources} folder.
     */
    public static String readResourceFile(String file) {
        InputStream is = getResourceAsStream(file);
        Scanner scanner = new Scanner(is, Const.SystemParams.ENCODING);
        try {
            return scanner.useDelimiter("\\Z").next();
        } finally {
            scanner.close();
        }
    }

}
