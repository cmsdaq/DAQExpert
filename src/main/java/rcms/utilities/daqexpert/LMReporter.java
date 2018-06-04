package rcms.utilities.daqexpert;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class LMReporter {

    private static final String CSV_FILE = "./logic-modules.csv";

    public static void main(String[] args) throws IOException {
        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_FILE));

                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("LM class name", "Title", "shorten", "Brief description", "Description"));

        ) {

            List<LogicModule> lms = LogicModuleRegistry.getModulesInRunOrder();

            for(LogicModule lm: lms) {
                csvPrinter.printRecord(lm.getClass().getSimpleName(), lm.getName(), lm.getDescription() != null? lm.getDescription().length() > 100? "TRUE":"":"", lm.getBriefDescription(), lm.getDescription());
            }

            csvPrinter.flush();
        }
    }
}

