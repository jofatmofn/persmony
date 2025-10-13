package org.sakuram.persmony.util;

public interface FlaggedEnum {
    default String getFlag() {
        return ((Enum<?>) this).name();
    }

    static <E extends Enum<E> & FlaggedEnum> E fromFlag(Class<E> enumClass, String flag) {
        if (flag == null)
            throw new IllegalArgumentException("Flag cannot be null");

        String normalized = flag.trim().toUpperCase();
        for (E e : enumClass.getEnumConstants()) {
            if (e.getFlag().trim().equalsIgnoreCase(normalized))
                return e;
        }
        throw new IllegalArgumentException("Unknown flag: " + flag + "' for " + enumClass.getSimpleName());
    }
}
