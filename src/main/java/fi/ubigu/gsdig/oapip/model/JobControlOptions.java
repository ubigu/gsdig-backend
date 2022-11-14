package fi.ubigu.gsdig.oapip.model;

public enum JobControlOptions {

    sync("sync-execute"),
    async("async-execute");

    private final String value;

    private JobControlOptions(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
