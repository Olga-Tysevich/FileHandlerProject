package by.filehandler.reader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileDataReader {

    List<Map<String, String>> readData(byte[] bytes) throws IOException;

    /**
     * Supported only for xlsx and xls formats.
     * @param sheetNumber Data Reading Sheet Number.
     */
    void setSheetNumber(int sheetNumber);

}
