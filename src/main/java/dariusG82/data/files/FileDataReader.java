package dariusG82.data.files;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static dariusG82.services.file_services.DataPath.SYSTEM_DATA_PATH;

public class FileDataReader {

    public ArrayList<String> getDataStrings() {
        try {
            Scanner scanner = new Scanner(new File(SYSTEM_DATA_PATH.getPath()));
            ArrayList<String> dataList = new ArrayList<>();

            while (scanner.hasNext()) {
                String data = scanner.nextLine();
                scanner.nextLine();
                dataList.add(data);
            }

            return dataList;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public void updateDataStrings(ArrayList<String> updatedDataStrings) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(SYSTEM_DATA_PATH.getPath()));

        for (String dataString : updatedDataStrings) {
            printWriter.println(dataString);
            printWriter.println();
        }

        printWriter.close();
    }
}
