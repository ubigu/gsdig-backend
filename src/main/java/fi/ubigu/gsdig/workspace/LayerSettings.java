package fi.ubigu.gsdig.workspace;

import java.util.UUID;

public class LayerSettings {

    private final UUID uuid;
    private final double opacity;
    private final boolean visible;

    public LayerSettings(UUID uuid, double opacity, boolean visible) {
        this.uuid = uuid;
        this.opacity = opacity;
        this.visible = visible;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getOpacity() {
        return opacity;
    }

    public boolean isVisible() {
        return visible;
    }

}
