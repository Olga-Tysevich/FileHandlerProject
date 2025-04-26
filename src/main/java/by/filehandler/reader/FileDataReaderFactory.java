package by.filehandler.reader;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileDataReaderFactory {

    public FileDataReader getFileDataReaderForType(FileType type) {

        return switch (type) {
            case XLSX -> new XlsxDataReader();
            case CSV -> new TxtDataReader(",");
            case TXT -> new TxtDataReader("\t");
            default -> throw new IllegalArgumentException("Unsupported file type: " + type);
        };
    }
}
