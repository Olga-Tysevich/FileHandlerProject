package by.filehandler.filter;

import by.filehandler.utils.MapUtils;
import by.filehandler.utils.SetUtils;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
public class PredicateRule implements Rule {
    private final Set<String> fieldNamesForTest;
    private final Predicate<String> rule;

    public boolean test(Map<String, String> fields) {
        List<String> forTest = fields.keySet().stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .filter(fn -> SetUtils.containsIgnoreCase(fieldNamesForTest, fn))
                .collect(Collectors.toList());

        boolean result = true;

        for (String field: forTest) {
            String value = MapUtils.getIgnoreCase(fields, field);
            if (!rule.test(value)) return false;
        }

        return result;
    }
}
