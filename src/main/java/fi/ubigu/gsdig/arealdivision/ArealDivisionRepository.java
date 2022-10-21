package fi.ubigu.gsdig.arealdivision;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ArealDivisionRepository {

    public List<ArealDivision> findAll() throws Exception;
    public List<ArealDivision> findAllPublic() throws Exception;
    public Optional<ArealDivision> findByUuid(UUID uuid) throws Exception;
    public Optional<ArealDivision> findPublicByUuid(UUID uuid) throws Exception;
    public void create(ArealDivision arealDivision) throws Exception;
    public Optional<ArealDivision> update(ArealDivision arealDivision) throws Exception;
    public boolean delete(UUID uuid) throws Exception;

}
