package by.filehandler.handler;

import by.filehandler.filter.DataFilter;
import by.filehandler.filter.PredicateRule;
import by.filehandler.filter.ValueInListRule;
import by.filehandler.reader.FileDataReader;
import by.filehandler.reader.FileType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;

import static by.filehandler.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class DefaultFileHandlerTest {

    private DefaultFileHandler fileHandler;
    private FileDataReader mockReader;

    @BeforeEach
    void setUp() {
        fileHandler = Mockito.spy(new DefaultFileHandler());
        mockReader = mock(FileDataReader.class);
    }

    private void mockDataReader(FileType fileType, List<Map<String, String>> mockData) throws IOException {
        doReturn(mockReader).when(fileHandler).dataReader(eq(fileType), any());
        when(mockReader.readData(any())).thenReturn(mockData);
    }

    @Test
    public void shouldSeparateIndividualsAndLegalEntities() throws IOException {
        List<Map<String, String>> mockData = List.of(
                Map.of(UPN_KEY_RU, EMPTY_VALUE, COMPANY_KEY, INDIVIDUALS_1),
                Map.of(UPN_KEY_RU, UNP_1, COMPANY_KEY, COMPANY_1)
        );

        mockDataReader(FileType.XLSX, mockData);

        DataFilter filter = new DataFilter();
        PredicateRule individualsRule = new PredicateRule(Set.of(UPN_KEY_RU.toLowerCase()), value -> value == null || value.trim().isEmpty());
        PredicateRule unpRule = new PredicateRule(Set.of(UPN_KEY_RU.toLowerCase()), value -> value != null && !value.trim().isEmpty());

        filter.addRule(INDIVIDUALS_KEY, individualsRule, List.of(COMPANY_KEY.toLowerCase()));
        filter.addRule(UPN_KEY_EN, unpRule, List.of(UPN_KEY_RU.toLowerCase()));

        Map<String, String[]> result = fileHandler.getDataArraysByColumns(FileType.XLSX, new byte[]{}, filter);

        Assertions.assertArrayEquals(new String[]{INDIVIDUALS_1}, result.get(INDIVIDUALS_KEY));
        Assertions.assertArrayEquals(new String[]{UNP_1}, result.get(UPN_KEY_EN));
    }

    @Test
    public void shouldExtractDataByFieldName() throws IOException {
        List<Map<String, String>> mockData = List.of(
                Map.of(UPN_KEY_RU, UNP_1),
                Map.of(UPN_KEY_RU, UNP_2)
        );

        mockDataReader(FileType.CSV, mockData);

        Set<String> result = fileHandler.extractDataByFieldName(FileType.CSV, new byte[]{}, UPN_KEY_RU, 0);

        assertEquals(Set.of(UNP_1, UNP_2), result);
    }

    @Test
    public void shouldExtractSelectedFieldsAfterFilter() throws IOException {
        List<Map<String, String>> mockData = List.of(
                Map.of(CUSTOMER_NAME_KEY, INDIVIDUALS_1, CUSTOMER_UPN_KEY, UNP_1),
                Map.of(CUSTOMER_NAME_KEY, INDIVIDUALS_3, CUSTOMER_UPN_KEY, UNP_2)
        );

        mockDataReader(FileType.TXT, mockData);

        DataFilter filter = new DataFilter();
        ValueInListRule unpRule = new ValueInListRule();
        unpRule.setFieldName(CUSTOMER_UPN_KEY);
        unpRule.setValues(Set.of(UNP_1));

        filter.addRule(CUSTOMER_UPN_KEY, unpRule, List.of(CUSTOMER_NAME_KEY, CUSTOMER_UPN_KEY));

        List<Map<String, String>> result = fileHandler.extractAllDataByFieldValues(FileType.TXT, new byte[]{}, filter);

        assertEquals(1, result.size());
        assertEquals(INDIVIDUALS_1, result.get(0).get(CUSTOMER_NAME_KEY));
        assertEquals(UNP_1, result.get(0).get(CUSTOMER_UPN_KEY));
    }

    @Test
    public void shouldExtractSelectedFieldsAfterFilterByClientName() throws IOException {
        List<Map<String, String>> mockData = List.of(
                Map.of(CUSTOMER_NAME_KEY, INDIVIDUALS_1, CUSTOMER_UPN_KEY, UNP_1, IGNORED_FIELD_KEY, IGNORED_FIELD_VAL_1),
                Map.of(CUSTOMER_NAME_KEY, INDIVIDUALS_2, CUSTOMER_UPN_KEY, UNP_2, IGNORED_FIELD_KEY, IGNORED_FIELD_VAL_2),
                Map.of(CUSTOMER_NAME_KEY, INDIVIDUALS_3, CUSTOMER_UPN_KEY, UNP_3, IGNORED_FIELD_KEY, IGNORED_FIELD_VAL_3)
        );

        mockDataReader(FileType.TXT, mockData);

        DataFilter filter = new DataFilter();
        PredicateRule nameRule = new PredicateRule(
                Set.of(CUSTOMER_NAME_KEY),
                value -> value != null && value.contains(INDIVIDUALS_NAME)
        );

        filter.addRule(CUSTOMER_NAME_KEY, nameRule, List.of(CUSTOMER_NAME_KEY, CUSTOMER_UPN_KEY));

        List<Map<String, String>> result = fileHandler.extractAllDataByFieldValues(FileType.TXT, new byte[]{}, filter);

        assertEquals(2, result.size());
        assertEquals(INDIVIDUALS_1, result.get(0).get(CUSTOMER_NAME_KEY));
        assertEquals(INDIVIDUALS_2, result.get(1).get(CUSTOMER_NAME_KEY));
        assertEquals(UNP_1, result.get(0).get(CUSTOMER_UPN_KEY));
        assertEquals(UNP_2, result.get(1).get(CUSTOMER_UPN_KEY));

        for (Map<String, String> record : result) {
            assertFalse(record.containsKey(IGNORED_FIELD_KEY));
        }
    }

    @Test
    public void shouldReturnEmptyMapWhenNoData() throws IOException {
        mockDataReader(FileType.XLSX, Collections.emptyList());

        DataFilter filter = new DataFilter();
        Map<String, String[]> result = fileHandler.getDataArraysByColumns(FileType.XLSX, new byte[]{}, filter);

        Assertions.assertTrue(result.isEmpty());
    }
}
