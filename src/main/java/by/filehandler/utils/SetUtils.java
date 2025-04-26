package by.filehandler.utils;

import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.Set;

@UtilityClass
public class SetUtils {

    public static boolean containsIgnoreCase(Set<String> set, String target) {
        if (Objects.isNull(set)) {
            return false;
        }
        for (String item : set) {
            if (Objects.nonNull(item) && item.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }
}
