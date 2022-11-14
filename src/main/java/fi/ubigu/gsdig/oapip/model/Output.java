package fi.ubigu.gsdig.oapip.model;

public class Output {

    private final Format format;
    private final TransmissionMode transmissionMode;

    public Output(Format format, TransmissionMode transmissionMode) {
        this.format = format;
        this.transmissionMode = transmissionMode;
    }

    public Format getFormat() {
        return format;
    }

    public TransmissionMode getTransmissionMode() {
        return transmissionMode;
    }

}
