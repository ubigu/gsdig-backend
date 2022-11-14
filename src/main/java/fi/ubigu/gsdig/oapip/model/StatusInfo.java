package fi.ubigu.gsdig.oapip.model;

import java.time.Instant;
import java.util.List;

import fi.ubigu.gsdig.joins.StatusCode;
import fi.ubigu.gsdig.oapi.model.Link;

public class StatusInfo {

    private final String processID;
    private final String type = "process";
    private final String jobID;
    private final StatusCode status;
    private final String message;
    private final Instant created;
    private final Instant started;
    private final Instant finished;
    private final Instant updated;
    private final Integer progress;
    private final List<Link> links;

    public StatusInfo(String processID, String jobID, StatusCode status, String message, Instant created, Instant started,
            Instant finished, Instant updated, Integer progress, List<Link> links) {
        this.processID = processID;
        this.jobID = jobID;
        this.status = status;
        this.message = message;
        this.created = created;
        this.started = started;
        this.finished = finished;
        this.updated = updated;
        this.progress = progress;
        this.links = links;
    }

    public String getProcessID() {
        return processID;
    }

    public String getType() {
        return type;
    }

    public String getJobID() {
        return jobID;
    }

    public StatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getStarted() {
        return started;
    }

    public Instant getFinished() {
        return finished;
    }

    public Instant getUpdated() {
        return updated;
    }

    public Integer getProgress() {
        return progress;
    }

    public List<Link> getLinks() {
        return links;
    }

    public StatusInfo withLinks(List<Link> links) {
        return new StatusInfo(processID, jobID, status, message, created, started, finished, updated, progress, links);
    }

}
