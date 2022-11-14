package fi.ubigu.gsdig.oapip;

import java.util.Map;

import fi.ubigu.gsdig.joins.StatusCode;

public class ProcessResponse {

    private final String mimeType;
    private final Map<String, Object> outputs;
    private final StatusCode status;

    public ProcessResponse(String mimeType, Map<String, Object> outputs, StatusCode status) {
        this.mimeType = mimeType;
        this.outputs = outputs;
        this.status = status;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public StatusCode getStatus() {
        return status;
    }

}
