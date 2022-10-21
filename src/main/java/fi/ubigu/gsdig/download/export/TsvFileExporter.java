package fi.ubigu.gsdig.download.export;

import org.springframework.stereotype.Component;

@Component("tsv")
public class TsvFileExporter extends SeparatedValueFileExporter implements FileExporter {

    public TsvFileExporter() {
        super(".tsv", "text/tab-separated-values", '\t');
    }

}
