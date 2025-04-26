package by.filehandler.filter;

import java.util.Map;

public interface Rule {

    boolean test(Map<String, String> fields);

}
