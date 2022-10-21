package fi.ubigu.gsdig.download.export;

import org.springframework.stereotype.Component;

@Component("csv")
public class CsvFileExporter extends SeparatedValueFileExporter implements FileExporter {

    public CsvFileExporter() {
        super(".csv", "text/csv", ',');
    }

}
