package fi.ubigu.gsdig.utility;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {

    public T map(ResultSet rs) throws Exception;

}
