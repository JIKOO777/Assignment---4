import util.CsvWriter;
import java.nio.file.*;
import java.util.*;

public class SanityCsv {
    public static void main(String[] args) throws Exception {
        Path out = Paths.get("out/test_sanity.csv");

        // Создаём файл и пишем заголовок
        CsvWriter.writeWithHeader(out,
                new String[]{"a","b","c"},
                Collections.emptyList());

        // Добавляем одну строку
        CsvWriter.append(out, Collections.singletonList(new String[]{"1","2","3"}));

        System.out.println("✅ CSV test done! Check out/test_sanity.csv");
    }
}
