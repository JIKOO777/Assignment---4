package util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvWriter {

    public static void writeWithHeader(Path file, String[] header, List<String[]> rows) throws IOException {
        Files.createDirectories(file.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            writeRow(w, header);
            for (String[] r : rows) writeRow(w, r);
        }
    }

    public static void append(Path file, List<String[]> rows) throws IOException {
        Files.createDirectories(file.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (String[] r : rows) writeRow(w, r);
        }
    }

    public static void writeRow(Writer w, String[] cols) throws IOException {
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) w.write(',');
            w.write(escape(cols[i] == null ? "" : cols[i]));
        }
        w.write(System.lineSeparator());
    }

    private static String escape(String s) {
        boolean need = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!need) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
