package by.filehandler.handler;

import by.filehandler.filter.DataFilter;
import by.filehandler.reader.FileDataReader;
import by.filehandler.reader.FileDataReaderFactory;
import by.filehandler.reader.FileType;
import by.filehandler.utils.MapUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DefaultFileHandler implements FileHandler {
    private final FileDataReaderFactory fileDataReaderFactory = new FileDataReaderFactory();

    @Override
    public Set<String> extractDataByFieldName(FileType fileType, byte[] bytes, String fieldName, Integer sheetNumber) {
        FileDataReader dataReader = dataReader(fileType, sheetNumber);
        try {
            List<Map<String, String>> data = dataReader.readData(bytes);
            return extractFieldValues(data, fieldName);
        } catch (IOException e) {
            log.error("Error extracting data by field name: {}", fieldName, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map<String, String>> extractDataByFieldValues(FileType fileType, byte[] bytes, DataFilter filter) {
        FileDataReader dataReader = dataReader(fileType, filter.getSheetNumber());
        try {
            List<Map<String, String>> data = dataReader.readData(bytes);
            return filterAndSelectFields(data, filter);
        } catch (IOException e) {
            log.error("Error extracting data by field values", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String[]> getDataArraysByColumns(FileType fileType, byte[] bytes, DataFilter filter) {
        FileDataReader dataReader = dataReader(fileType, filter.getSheetNumber());
        try {
            List<Map<String, String>> data = dataReader.readData(bytes);
            return groupDataByFilterRules(data, filter);
        } catch (IOException e) {
            log.error("Error getting data arrays by columns", e);
            throw new RuntimeException(e);
        }
    }

    protected Map<String, String[]> convertToFinalFormat(Map<String, List<String>> input) {
        Map<String, String[]> output = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : input.entrySet()) {
            List<String> valueList = entry.getValue();
            output.put(entry.getKey(), valueList.toArray(new String[0]));
        }
        return output;
    }

    FileDataReader dataReader(FileType fileType, Integer sheetNumber) {
        FileDataReader dataReader = fileDataReaderFactory.getFileDataReaderForType(fileType);
        int sheetNo = Objects.requireNonNullElse(sheetNumber, 0);
        dataReader.setSheetNumber(sheetNo);
        return dataReader;
    }

    private Set<String> extractFieldValues(List<Map<String, String>> data, String fieldName) {
        Set<String> fieldValues = new HashSet<>();
        for (Map<String, String> row : data) {
            String value = MapUtils.getIgnoreCase(row, fieldName);
            if (value != null) {
                fieldValues.add(value);
            }
        }
        return fieldValues;
    }

    private List<Map<String, String>> filterAndSelectFields(List<Map<String, String>> data, DataFilter filter) {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, String> row : data) {
            Set<String> passedRuleKeys = filter.checkRules(row);
            List<Map<String, String>> filteredRow = prepareRow(row, passedRuleKeys, filter).stream()
                    .filter(m -> !m.isEmpty())
                    .collect(Collectors.toList());
            result.addAll(filteredRow);
        }
        return result;
    }

    private List<Map<String, String>> prepareRow(Map<String, String> row, Set<String> passedRuleKeys, DataFilter filter) {
        List<Map<String, String>> filteredRow = new ArrayList<>();
        if (Objects.nonNull(passedRuleKeys) && !passedRuleKeys.isEmpty()) {
            for (String passedRuleKey : passedRuleKeys) {
                List<String> fieldListToInclude = filter.getFields(passedRuleKey);
                List<String> allFields = new ArrayList<>(row.keySet());
                List<String> fieldsToInclude = fieldListToInclude.isEmpty()? allFields : fieldListToInclude;
                filteredRow.add(copySelectedFields(row, fieldsToInclude, new HashMap<>()));
            }
        }

        return filteredRow;
    }

    private Map<String, String> copySelectedFields(Map<String, String> sourceRow, List<String> fields, Map<String, String> targetRow) {
        for (String field : fields) {
            String value = MapUtils.getIgnoreCase(sourceRow, field);
            if (Objects.nonNull(value)) {
                targetRow.put(field, value);
            }
        }
        return targetRow;
    }

    private Map<String, String[]> groupDataByFilterRules(List<Map<String, String>> data, DataFilter filter) {
        Map<String, List<String>> groupedData = new HashMap<>();
        for (Map<String, String> row : data) {
            Set<String> resultFields = filter.checkRules(row);
            addRowToGroupedData(row, resultFields, filter, groupedData);
        }
        return convertToFinalFormat(groupedData);
    }

    private void addRowToGroupedData(Map<String, String> row, Set<String> resultFields, DataFilter filter, Map<String, List<String>> groupedData) {
        for (String resultField : resultFields) {
            List<String> resultColumns = filter.getFields(resultField);
            for (String resultColumn : resultColumns) {
                if (MapUtils.containsKeyIgnoreCase(row, resultColumn)) {
                    String value = MapUtils.getIgnoreCase(row, resultColumn);
                    groupedData.computeIfAbsent(resultField, k -> new ArrayList<>()).add(value);
                }
            }
        }
    }
}
