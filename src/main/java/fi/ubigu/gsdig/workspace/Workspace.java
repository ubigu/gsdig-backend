package fi.ubigu.gsdig.workspace;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Workspace {

    private UUID uuid;
    private String title;
    private UUID createdBy;
    private Instant createdAt;
    private Instant lastModifiedAt;
    private double[] center;
    private double zoom;
    private List<LayerSettings> backgroundLayers;
    private LayerSettings dataLayer;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Instant lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public double[] getCenter() {
        return center;
    }

    public void setCenter(double[] center) {
        this.center = center;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public List<LayerSettings> getBackgroundLayers() {
        return backgroundLayers;
    }

    public void setBackgroundLayers(List<LayerSettings> backgroundLayers) {
        this.backgroundLayers = backgroundLayers;
    }

    public LayerSettings getDataLayer() {
        return dataLayer;
    }

    public void setDataLayer(LayerSettings dataLayer) {
        this.dataLayer = dataLayer;
    }

}
