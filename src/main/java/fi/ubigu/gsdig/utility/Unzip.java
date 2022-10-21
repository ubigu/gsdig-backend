package fi.ubigu.gsdig.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.util.FileSystemUtils;

public class Unzip {
    
    private static final byte[] ZIP_HEADER = new byte[] { 0x50, 0x4b, 0x03, 0x04 };
    private static final int MAX_NUMBER_OF_ZIP_ENTRIES = 10;
    
    public static File unzipIfZipFile(File file, File dir) throws Exception {
        if (!Arrays.equals(ZIP_HEADER, Utils.readFirstNBytes(file, ZIP_HEADER.length))) {
            // Not zip file
            return file;
        }

        try {
            unzip(file, dir, MAX_NUMBER_OF_ZIP_ENTRIES);
        } catch (Exception e) {
            // Cleanup the partly unzipped created directory
            if (dir.exists()) {
                FileSystemUtils.deleteRecursively(dir);
            }
            throw e;
        }
        file.delete(); // Delete the zip file after succesfully unzipping it
        return dir;
    }
    
    public static void unzip(File zipFile, File destDir, int maxNumberOfEntries) throws IOException {
        byte[] buf = new byte[1024];
        int i = 0;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (++i == maxNumberOfEntries) {
                    throw new IllegalArgumentException("Zip files contains too many entries!");
                }
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                    
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buf)) > 0) {
                            fos.write(buf, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    /**
     * Avoid Zip Slip
     * @see https://snyk.io/research/zip-slip-vulnerability
     */
    private static File newFile(File destDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destDir, zipEntry.getName());

        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

}
