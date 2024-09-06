package dev.kiddo.ingameshopdir.client.utils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvParser implements AutoCloseable {

    // Regular expression for CSV fields that handles quotes
    private static final Pattern CSV_PATTERN = Pattern.compile(
            "\"([^\"]*)\"|([^,]+)"
    );
    private final BufferedReader reader;
    private final List<CsvRecord> records;
    private int lineNumber;

    public CsvParser(String filePath) throws IOException {
        this.reader = new BufferedReader(new FileReader(filePath));
        this.records = new ArrayList<>();
        this.lineNumber = 1;
    }

    public Iterable<CsvRecord> parse() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                List<String> fields = new ArrayList<>();
                Matcher matcher = CSV_PATTERN.matcher(trimmedLine);
                while (matcher.find()) {
                    if (matcher.group(1) != null) {
                        // Quoted field
                        fields.add(matcher.group(1));
                    } else {
                        // Non-quoted field
                        fields.add(matcher.group(2));
                    }
                }
                records.add(new CsvRecordImpl(lineNumber++, fields.toArray(new String[0])));
            }
        }
        return records;
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
