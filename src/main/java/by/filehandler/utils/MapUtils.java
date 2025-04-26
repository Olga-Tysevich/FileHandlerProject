package by.filehandler.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Objects;

@UtilityClass
public class MapUtils {

    public static String getIgnoreCase(Map<String, String> map, String key) {
        if (Objects.isNull(key) || Objects.isNull(map)) return null;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Objects.nonNull(entry.getKey()) && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static boolean containsKeyIgnoreCase(Map<String, ?> map, String key) {
        if (Objects.isNull(key) || Objects.isNull(map)) return false;
        for (String mapKey : map.keySet()) {
            if (Objects.nonNull(mapKey) && mapKey.equalsIgnoreCase(key)) {
                return true;
            }
        }

        return false;
    }
}
