package fi.ubigu.gsdig.utility;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface StatementPreparer {
    
    public void prepare(PreparedStatement ps) throws Exception;

}
