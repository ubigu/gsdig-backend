package fi.ubigu.gsdig.download.export;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.springframework.stereotype.Component;

@Component("xlsx")
public class XlsxFileExporter extends BaseFileExporter implements FileExporter {

    protected XlsxFileExporter() {
        super(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Override
    public File export(File dir, SimpleFeatureCollection fc) throws Exception {
        List<String> attributes = fc.getSchema().getAttributeDescriptors()
                .stream()
                .filter(it -> !(it instanceof GeometryDescriptor))
                .map(it -> it.getLocalName())
                .collect(Collectors.toList());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(fc.getSchema().getTypeName());

        final int n = attributes.size();
        int rownum = 0;
        Row row = sheet.createRow(rownum++);

        for (int i = 0; i < n; i++) {
            row.createCell(i).setCellValue(attributes.get(i));
        }

        try (SimpleFeatureIterator it = fc.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                row = sheet.createRow(rownum++);
                for (int i = 0; i < n; i++) {
                    Object o = f.getAttribute(attributes.get(i));
                    Cell cell = row.createCell(i);
                    if (o == null) {
                        cell.setBlank();
                    } else if (o instanceof Number) {
                        cell.setCellValue(((Number) o).doubleValue());
                    } else {
                        cell.setCellValue(o.toString());
                    }
                }
            }
        }

        File file = File.createTempFile("tmp", ".xslx", dir);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
            workbook.close();
        }
        return file;
    }

}
