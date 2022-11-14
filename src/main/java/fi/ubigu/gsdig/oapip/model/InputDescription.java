package fi.ubigu.gsdig.oapip.model;

import java.util.List;

import io.swagger.v3.oas.models.media.Schema;

@SuppressWarnings("rawtypes")
public class InputDescription extends DescriptionType {

    private final Integer minOccurs;
    private final Integer maxOccurs;
    private final Schema schema;

    public InputDescription(String title, String description, List<String> keywords, List<Metadata> metadata,
            Integer minOccurs, Integer maxOccurs, Schema schema) {
        super(title, description, keywords, metadata);
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.schema = schema;
    }

    public Integer getMinOccurs() {
        return minOccurs;
    }

    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public Schema getSchema() {
        return schema;
    }

}
