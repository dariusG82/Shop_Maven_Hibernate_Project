package testing;

import java.time.LocalDate;

public class MainTest {

    public static void main(String[] args) {
        String date = "2022-05-01";
        LocalDate localDate = LocalDate.parse(date);
        System.out.println(localDate.getYear());
    }
}
