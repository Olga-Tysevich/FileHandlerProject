package by.filehandler.filter;

import lombok.Data;

import java.util.*;

@Data
public class DataFilter {
    private final Map<String, List<Rule>> rules = new HashMap<>();
    private final Map<String, List<String>> resultFields = new HashMap<>();
    private Integer sheetNumber;

    public void addRule(String resultFieldName, Rule rule, List<String> fields) {
        List<Rule> rulesForField = rules.getOrDefault(resultFieldName, new ArrayList<>());
        rulesForField.add(rule);
        rules.put(resultFieldName, rulesForField);

        List<String> resultFieldNames = resultFields.getOrDefault(resultFieldName, new ArrayList<>());
        resultFieldNames.addAll(Objects.requireNonNullElse(fields, new ArrayList<>()));
        resultFields.put(resultFieldName, resultFieldNames);
    }


    public Set<String> checkRules(Map<String, String> fields) {
        Set<String> resultFields = new HashSet<>();

        for (Map.Entry<String, List<Rule>> entry : rules.entrySet()) {
            for (Rule rule : entry.getValue()) {
                if (rule.test(fields))
                    resultFields.add(entry.getKey());
            }
        }

        return resultFields;
    }

    public List<String> getFields(String resultFieldName) {
        return resultFields.getOrDefault(resultFieldName, new ArrayList<>());
    }

}