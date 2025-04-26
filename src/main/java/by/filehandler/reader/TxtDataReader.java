package by.filehandler.reader;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@AllArgsConstructor
@Data
public class TxtDataReader implements FileDataReader {
    private String delimiter;

    @Override
    public List<Map<String, String>> readData(byte[] bytes) {
        List<Map<String, String>> result = new ArrayList<>();

        String content = tryDecode(bytes, StandardCharsets.UTF_8);

        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }

        String[] lines = content.split("\n");

        if (lines.length > 0) {;
            String[] headers = lines[0].split(delimiter, -1);

            for (int i = 1; i < lines.length; i++) {
                String[] fields = lines[i].split(delimiter, -1);
                fields = Arrays.stream(fields)
                        .map(String::trim)
                        .toArray(String[]::new);
                Map<String, String> rowData = new HashMap<>();

                for (int j = 0; j < headers.length && j < fields.length; j++) {
                    String header = headers[j].trim();
                    String value = fields[j].trim();
                    if (!header.isEmpty()) {
                        rowData.put(header, value);
                    }
                }

                if (!rowData.isEmpty()) {
                    result.add(rowData);
                }
            }
        }

        return result;
    }

    @Override
    public void setSheetNumber(int sheetNumber) {
        System.out.println("Not supported operation.");
    }

    private String tryDecode(byte[] bytes, Charset charset) {
        try {
            return new String(bytes, charset);
        } catch (Exception e) {
            return "";
        }
    }
}
