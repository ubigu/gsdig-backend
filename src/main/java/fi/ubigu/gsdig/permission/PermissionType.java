package fi.ubigu.gsdig.permission;

import java.util.EnumSet;

public enum PermissionType {
    
    READ(1),
    WRITE(2);
    
    private final int code;

    private PermissionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static final EnumSet<PermissionType> ALL = EnumSet.allOf(PermissionType.class);

}
