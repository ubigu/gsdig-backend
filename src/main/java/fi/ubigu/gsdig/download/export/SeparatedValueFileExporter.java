package fi.ubigu.gsdig.download.export;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;

public class SeparatedValueFileExporter extends BaseFileExporter implements FileExporter {
    
    private final char sep;

    public SeparatedValueFileExporter(String fileExtension, String contentType, char sep) {
        super(fileExtension, contentType);
        this.sep = sep;
    }

    @Override
    public File export(File dir, SimpleFeatureCollection fc) throws Exception {
        File file = File.createTempFile("tmp", ".csv", dir);

        List<String> attributes = fc.getSchema().getAttributeDescriptors()
                .stream()
                .filter(it -> !(it instanceof GeometryDescriptor))
                .map(it -> it.getLocalName())
                .collect(Collectors.toList());
        
        try (BufferedWriter out = Files.newBufferedWriter(file.toPath());
                SimpleFeatureIterator it = fc.features()) {
            final int n = attributes.size();
            
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    out.write(sep);
                }
                out.write(attributes.get(i));
            }
            out.write('\n');
            
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                for (int i = 0; i < n; i++) {
                    if (i > 0) {
                        out.write(sep);
                    }
                    Object o = f.getAttribute(attributes.get(i));
                    if (o != null) {
                        if (o instanceof Number) {
                            out.write(o.toString());
                        } else {
                            out.write(csv(o.toString()));
                        }
                    }
                }
                out.write('\n');
            }
        }

        return file;
    }
    
    private String csv(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '"') {
                sb.append('"');
                sb.append('"');
            } else if (ch == '\r') {
                sb.append('\\');
                sb.append('r');
            } else if (ch == '\n') {
                sb.append('\\');
                sb.append('n');
            } else {
                sb.append(ch);
            }
        }
        sb.append('"');
        return sb.toString();
    }

}
