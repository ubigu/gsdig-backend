package fi.ubigu.gsdig.unitdata;

import java.util.Map;
import java.util.UUID;

import fi.ubigu.gsdig.arealdivision.ArealDivision;
import fi.ubigu.gsdig.arealdivision.AttributeInfo;

public class UnitDataset extends ArealDivision {

    private final boolean remote;
    private final SensitivitySetting sensitivitySetting;

    public UnitDataset(UUID uuid, UUID createdBy, String title, String description, String organization, boolean publicity,
            double[] extent, Map<String, AttributeInfo> attributes, boolean remote, SensitivitySetting sensitivitySetting) {
        super(uuid, createdBy, title, description, organization, publicity, extent, attributes);
        this.remote = remote;
        this.sensitivitySetting = sensitivitySetting;
    }

    public boolean isRemote() {
        return remote;
    }

    public SensitivitySetting getSensitivitySetting() {
        return sensitivitySetting;
    }

    @Override
    public UnitDataset withUuid(UUID randomUUID) {
        return new UnitDataset(randomUUID, createdBy, title, description, organization, publicity, extent, attributes, remote, sensitivitySetting);
    }

    @Override
    public UnitDataset withCreatedBy(UUID userId) {
        return new UnitDataset(uuid, userId, title, description, organization, publicity, extent, attributes, remote, sensitivitySetting);
    }

}
