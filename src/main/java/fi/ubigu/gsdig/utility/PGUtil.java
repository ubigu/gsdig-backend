package fi.ubigu.gsdig.utility;

import java.security.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.type.AttributeDescriptor;
import org.postgresql.util.PGobject;

public class PGUtil {

    public static PGobject toJsonObject(String json) throws Exception {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("json");
        jsonObject.setValue(json);
        return jsonObject;
    }
    
    public static String toCreateTableNotation(AttributeDescriptor attribute) {
        return attribute.getLocalName() + " " + getPostgresType(attribute.getType().getBinding());
    }
    
    @SuppressWarnings("serial")
    public static final Map<Class<?>, String> BINDING_TO_DB_TYPE = new HashMap<Class<?>, String>() {
        {
            put(Byte.class, "int2");
            put(Short.class, "int2");
            put(Integer.class, "int4");
            put(Long.class, "int8");
            put(Float.class, "float4");
            put(Double.class, "float8");
            put(String.class, "text");
            put(Boolean.class, "boolean");
            put(Date.class, "date");
            put(Time.class, "time");
            put(Timestamp.class, "timestamp");
            put(byte[].class, "bytea");
        }
    };
    
    public static String getPostgresType(Class<?> binding) {
        return BINDING_TO_DB_TYPE.get(binding);
    }

}
