package fi.ubigu.gsdig.utility;

import java.io.File;
import java.io.IOException;

public class FileSafety {

    /**
     * Avoid Zip Slip / other malicious issues
     * @see https://snyk.io/research/zip-slip-vulnerability
     */
    public static File newFile(File destDir, String name) throws IOException {
        File destFile = new File(destDir, name);

        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IllegalArgumentException("Entry is outside of the target dir: " + name);
        }

        return destFile;
    }

}
