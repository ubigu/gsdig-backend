package fi.ubigu.gsdig.background;

import java.util.Map;
import java.util.UUID;

public class BackgroundLayer {

    private final UUID uuid;
    private final String type;
    private final String title;
    private final Map<String, Object> options;

    public BackgroundLayer(UUID uuid, String type, String title, Map<String, Object> options) {
        this.uuid = uuid;
        this.type = type;
        this.title = title;
        this.options = options;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

}
