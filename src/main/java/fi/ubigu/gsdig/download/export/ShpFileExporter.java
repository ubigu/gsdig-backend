package fi.ubigu.gsdig.download.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.springframework.stereotype.Component;

@Component("shp")
public class ShpFileExporter extends BaseFileExporter implements FileExporter {

    protected ShpFileExporter() {
        super(".zip", "application/zip");
    }

    @Override
    public File export(File dir, SimpleFeatureCollection featureCollection) throws Exception {
        ShapefileDumper dumper = new ShapefileDumper(dir);
        dumper.setCharset(StandardCharsets.UTF_8);
        dumper.dump(featureCollection);

        String name = featureCollection.getSchema().getTypeName();
        File[] files = copyCstCpg(dir);
        return zipFiles(files, new File(dir, name + ".zip"));
    }

    private static File zipFiles(File[] filesToZip, File zipFile) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (File f : filesToZip) {
                ZipEntry ze = new ZipEntry(f.getName());
                zos.putNextEntry(ze);
                Files.copy(f.toPath(), zos);
                zos.closeEntry();
            }
        }
        return zipFile;
    }

    private File[] copyCstCpg(File dir) throws IOException {
        File[] files = dir.listFiles();
        List<File> cstFiles = Arrays.stream(dir.listFiles())
                .filter(f -> f.getName().endsWith(".cst"))
                .collect(Collectors.toList());

        if (cstFiles.isEmpty()) {
            return files;
        }

        for (File cst : cstFiles) {
            String name = cst.getName().substring(0, cst.getName().length() - 3) + "cpg";
            File cpg = new File(dir, name);
            Files.copy(cst.toPath(), cpg.toPath());
        }
        return dir.listFiles();
    }

}
