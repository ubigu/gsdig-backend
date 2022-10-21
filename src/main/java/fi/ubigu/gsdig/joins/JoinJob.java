package fi.ubigu.gsdig.joins;

import java.time.Instant;
import java.util.UUID;

public class JoinJob {

    private UUID uuid;
    private UUID createdBy;
    private JoinRequest request;
    private StatusCode status;
    private String message;
    private Instant created;
    private Instant started;
    private Instant finished;
    private Instant updated;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public JoinRequest getRequest() {
        return request;
    }

    public void setRequest(JoinRequest request) {
        this.request = request;
    }

    public StatusCode getStatus() {
        return status;
    }

    public void setStatus(StatusCode status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getStarted() {
        return started;
    }

    public void setStarted(Instant started) {
        this.started = started;
    }

    public Instant getFinished() {
        return finished;
    }

    public void setFinished(Instant finished) {
        this.finished = finished;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

}
