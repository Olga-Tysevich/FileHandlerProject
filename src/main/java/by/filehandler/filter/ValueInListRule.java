package by.filehandler.filter;

import by.filehandler.utils.MapUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueInListRule implements Rule {
    private String fieldName;
    private Set<String> values;

    @Override
    public boolean test(Map<String, String> fields) {
        if (MapUtils.containsKeyIgnoreCase(fields, fieldName)) {
            String fieldValue = MapUtils.getIgnoreCase(fields, fieldName);
            return values.contains(fieldValue);
        }
        return false;
    }
}
