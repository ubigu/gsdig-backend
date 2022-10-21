package fi.ubigu.gsdig.unitdata;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface UnitDataRepository {

    public List<UnitDataset> findAll() throws Exception;
    public List<UnitDataset> findAllPublic() throws Exception;

    public Optional<UnitDataset> findByUuid(UUID uuid) throws Exception;
    public Optional<UnitDataset> findPublicByUuid(UUID uuid) throws Exception;

    public void create(UnitDataset metadata) throws Exception;
    public boolean update(UnitDataset metadata) throws Exception;
    public Optional<UnitDataset> delete(UUID uuid) throws Exception;

}
