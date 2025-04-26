package by.filehandler.reader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XlsxDataReader implements FileDataReader {
    private int sheetNumber;

    @Override
    public List<Map<String, String>> readData(byte[] bytes) throws IOException {
        try (InputStream is = new ByteArrayInputStream(bytes);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(sheetNumber);
            List<Map<String, String>> result = new ArrayList<>();

            Row header = sheet.getRow(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < header.getLastCellNum(); j++) {
                    String key = header.getCell(j).getStringCellValue();

                    if (Objects.isNull(key) || key.isBlank() || key.equals("=")) {
                        continue;
                    }

                    String value = getCellValue(row.getCell(j));
                    rowData.put(key, value);
                }
                result.add(rowData);
            }
            return result;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case FORMULA -> cell.getCellFormula();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}