package fi.ubigu.gsdig.oapip.model;

import java.util.Map;

public class Execute {

    private Map<String, InlineOrRefData> inputs;
    private Map<String, Output> outputs;
    private Response response = Response.raw;

    public Map<String, InlineOrRefData> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, InlineOrRefData> inputs) {
        this.inputs = inputs;
    }

    public Map<String, Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, Output> outputs) {
        this.outputs = outputs;
    }

    public Response getResponse() {
        return response;
    }

}
