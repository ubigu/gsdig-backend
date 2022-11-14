package fi.ubigu.gsdig.oapip.model;

import java.util.List;

import io.swagger.v3.oas.models.media.Schema;

@SuppressWarnings("rawtypes")
public class OutputDescription extends DescriptionType {
    
    private final Schema schema;

    public OutputDescription(String title, String description, List<String> keywords, List<Metadata> metadata,
            Schema schema) {
        super(title, description, keywords, metadata);
        this.schema = schema;
    }

    public Schema getSchema() {
        return schema;
    }

}
