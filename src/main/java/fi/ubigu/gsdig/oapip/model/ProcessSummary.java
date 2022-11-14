package fi.ubigu.gsdig.oapip.model;

import java.util.List;

import fi.ubigu.gsdig.oapi.model.Link;

public class ProcessSummary extends DescriptionType {
    
    private final String id;
    private final String version;
    private final List<JobControlOptions> jobControlOptions;
    private final List<TransmissionMode> outputTransmission;
    private final List<Link> links;
    
    public ProcessSummary(String id,
            String version, 
            String title,
            String description,
            List<String> keywords,
            List<Metadata> metadata,
            List<JobControlOptions> jobControlOptions,
            List<TransmissionMode> outputTransmission,
            List<Link> links) {
        super(title, description, keywords, metadata);
        this.id = id;
        this.version = version;
        this.jobControlOptions = jobControlOptions;
        this.outputTransmission = outputTransmission;
        this.links = links;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public List<JobControlOptions> getJobControlOptions() {
        return jobControlOptions;
    }

    public List<TransmissionMode> getOutputTransmission() {
        return outputTransmission;
    }

    public List<Link> getLinks() {
        return links;
    }
    
}
