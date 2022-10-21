package fi.ubigu.gsdig.workspace.defaults;

import fi.ubigu.gsdig.workspace.LayerSettings;

public class WorkspaceDefaults {

    private final String title;
    private final double[] center;
    private final double zoom;
    private final LayerSettings backgroundLayer;

    public WorkspaceDefaults(String title, double[] center, double zoom, LayerSettings backgroundLayer) {
        this.title = title;
        this.center = center;
        this.zoom = zoom;
        this.backgroundLayer = backgroundLayer;
    }

    public String getTitle() {
        return title;
    }

    public double[] getCenter() {
        return center;
    }

    public double getZoom() {
        return zoom;
    }

    public LayerSettings getBackgroundLayer() {
        return backgroundLayer;
    }

}
