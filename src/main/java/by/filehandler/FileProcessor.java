package by.filehandler;

import by.filehandler.filter.DataFilter;
import by.filehandler.filter.PredicateRule;
import by.filehandler.filter.ValueInListRule;
import by.filehandler.handler.DefaultFileHandler;
import by.filehandler.handler.FileHandler;
import by.filehandler.reader.FileType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Demo application!
 */
public class FileProcessor {
    private static final String DOMAIN_PATH_CVS = "src/main/resources/cvs/domains.csv";
    private static final String RES_PATH_CVS = "src/main/resources/cvs/res.csv";
    private static final String UNP_PATH_CVS = "src/main/resources/cvs/УНП.csv";

    private static final String DOMAIN_PATH_XLSX = "src/main/resources/excel/domains.xlsx";
    private static final String RES_PATH_XLSX = "src/main/resources/excel/res.xls";
    private static final String UNP_PATH_XLSX = "src/main/resources/excel/УНП.xlsx";

    private static final String DOMAIN_PATH_TXT = "src/main/resources/txt/domains.txt";
    private static final String RES_PATH_TXT = "src/main/resources/txt/res.txt";
    private static final String UNP_PATH_TXT = "src/main/resources/txt/УНП.txt";

    public static void processFiles(FileType fileType, String domainPath, String resPath, String unpPath, FileHandler fileHandler) throws Exception {
        byte[] readDomainData = Files.readAllBytes(Paths.get(domainPath));
        byte[] readResData = Files.readAllBytes(Paths.get(resPath));

        System.out.println("DOMAIN_PATH: " + domainPath);
        System.out.println("RES_PATH: " + resPath);

        int sheetNumber = RES_PATH_XLSX.equals(resPath) ? 1 : 0;
        List<Map<String, String>> extractedData = fileHandler.extractAllDataByFieldValues(fileType, readResData,
                toReadAllData(fileType, fileHandler, unpPath, sheetNumber));

        Map<String, String[]> dataArrays = fileHandler.getDataArraysByColumns(fileType, readDomainData, toSeparateData());

        System.out.println("Extracted Data: ");
        for (Map<String, String> entry : extractedData) {
            System.out.println(entry);
        }

        System.out.println("\nData Arrays: ");
        for (Map.Entry<String, String[]> entry : dataArrays.entrySet()) {
            System.out.println(entry.getKey() + ": " + String.join(", ", entry.getValue()));
        }
    }

    public static DataFilter toSeparateData() {
        DataFilter toSeparateData = new DataFilter();
        PredicateRule individualsPredicateRule = new PredicateRule(Set.of("УНП".toLowerCase()), value -> value == null || value.trim().isEmpty());
        PredicateRule unpPredicatePredicateRule = new PredicateRule(Set.of("УНП".toLowerCase()), value -> value != null && !value.trim().isEmpty());
        toSeparateData.addRule("individuals", individualsPredicateRule, List.of("Организация".toLowerCase()));
        toSeparateData.addRule("unp", unpPredicatePredicateRule, List.of("УНП".toLowerCase()));
        return toSeparateData;
    }

    public static DataFilter toReadAllData(FileType fileType, FileHandler fileHandler, String filePath,int sheetNumber) throws Exception {
        DataFilter toReadAllData = new DataFilter();
        ValueInListRule readAllDataByUNP = new ValueInListRule();

        System.out.println("UNP_PATH: " + filePath);
        byte[] readUnpData = Files.readAllBytes(Paths.get(filePath));
        Set<String> unpList = fileHandler.extractDataByFieldName(fileType, readUnpData, "УНП", 0);

        readAllDataByUNP.setFieldName("УНП клиента");
        readAllDataByUNP.setValues(unpList);
        toReadAllData.addRule("УНП клиента", readAllDataByUNP, null);

        toReadAllData.setSheetNumber(sheetNumber);

        return toReadAllData;
    }

    public static void main(String[] args) throws Exception {


        FileHandler fileHandler = new DefaultFileHandler();

        processFiles(FileType.CSV, DOMAIN_PATH_CVS, RES_PATH_CVS, UNP_PATH_CVS, fileHandler);
        processFiles(FileType.XLSX, DOMAIN_PATH_XLSX, RES_PATH_XLSX, UNP_PATH_XLSX, fileHandler);
        processFiles(FileType.TXT, DOMAIN_PATH_TXT, RES_PATH_TXT, UNP_PATH_TXT, fileHandler);
    }
}
