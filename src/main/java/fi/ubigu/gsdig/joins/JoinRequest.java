package fi.ubigu.gsdig.joins;

import java.util.List;
import java.util.UUID;

public class JoinRequest {

    private String title;
    private String description;
    private UUID arealDivision;
    private List<String> areaAttributes;
    private UUID unitDataset;
    private List<JoinAttribute> dataAttributes;
    private String additionalGroupingProperty;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
        
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getArealDivision() {
        return arealDivision;
    }

    public void setArealDivision(UUID arealDivision) {
        this.arealDivision = arealDivision;
    }

    public List<String> getAreaAttributes() {
        return areaAttributes;
    }

    public void setAreaAttributes(List<String> areaAttributes) {
        this.areaAttributes = areaAttributes;
    }

    public UUID getUnitDataset() {
        return unitDataset;
    }

    public void setUnitDataset(UUID unitDataset) {
        this.unitDataset = unitDataset;
    }

    public List<JoinAttribute> getDataAttributes() {
        return dataAttributes;
    }

    public void setDataAttributes(List<JoinAttribute> dataAttributes) {
        this.dataAttributes = dataAttributes;
    }

    public String getAdditionalGroupingProperty() {
        return additionalGroupingProperty;
    }

    public void setAdditionalGroupingProperty(String additionalGroupingProperty) {
        this.additionalGroupingProperty = additionalGroupingProperty;
    }

}
