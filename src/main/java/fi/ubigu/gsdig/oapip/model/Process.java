package fi.ubigu.gsdig.oapip.model;

import java.util.List;
import java.util.Map;

import fi.ubigu.gsdig.oapi.model.Link;

public class Process extends ProcessSummary {
    
    private final Map<String, InputDescription> inputs;
    private final Map<String, OutputDescription> outputs;

    public Process(String id, String version, String title, String description, List<String> keywords,
            List<Metadata> metadata, List<JobControlOptions> jobControlOptions,
            List<TransmissionMode> outputTransmission, List<Link> links,
            Map<String, InputDescription> inputs,
            Map<String, OutputDescription> outputs) {
        super(id, version, title, description, keywords, metadata, jobControlOptions, outputTransmission, links);
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public Map<String, InputDescription> getInputs() {
        return inputs;
    }

    public Map<String, OutputDescription> getOutputs() {
        return outputs;
    }

}
