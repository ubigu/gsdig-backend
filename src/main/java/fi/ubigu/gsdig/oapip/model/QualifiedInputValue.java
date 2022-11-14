package fi.ubigu.gsdig.oapip.model;

public final class QualifiedInputValue extends Format implements InlineOrRefData {

    private final InputValue value;

    public QualifiedInputValue(String mediaType, String encoding, String schema, InputValue value) {
        super(mediaType, encoding, schema);
        this.value = value;
    }

    public InputValue getValue() {
        return value;
    }

}
