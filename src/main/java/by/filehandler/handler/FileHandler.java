package by.filehandler.handler;

import by.filehandler.filter.DataFilter;
import by.filehandler.reader.FileType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FileHandler {

    Set<String> extractDataByFieldName(FileType fileType, byte[] bytes, String fieldName, Integer sheetNumber);

    List<Map<String, String>> extractDataByFieldValues(FileType fileType, byte[] bytes, DataFilter filter);

    Map<String, String[]> getDataArraysByColumns(FileType fileType, byte[] bytes, DataFilter filter);

}
