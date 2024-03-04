import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Comparator;

public class hbReader {
    public static String HBfileName = "happy.txt";

    public static Map<String, ArrayList<String>> hbDtoN = new HashMap<>();
    public static Map<String, ArrayList<String>> hbNtoD = new HashMap<>();
    public static ArrayList<String> datas = new ArrayList<>();

    public static void get(int dayOffset, MyBot myBot){
        hbDtoN = new HashMap<>();
        hbNtoD = new HashMap<>();
        datas = new ArrayList<>();
        hbReader.load(HBfileName, hbDtoN, hbNtoD, datas);
        ArrayList<String> today = new ArrayList<>();
        ArrayList<String> close = new ArrayList<>();
        int shortest = 500, i = 0, c = 0;

        for (String s : datas) {
            if (hbReader.getDayDiff(s) == 0) {
                ArrayList<String> temp = hbDtoN.get(s);
                for (String name : temp){
                    today.add(name + " -> Today " + hbReader.getYearDiff(s) + " " + hbReader.getPostfixY(hbReader.getYearDiff(s)));
                }
            } else if (hbReader.getDayDiff(s) <= dayOffset) {
                ArrayList<String> temp = hbDtoN.get(s);
                for (String name : temp){
                    close.add(name + ": after " + hbReader.getDayDiff(s) + " " + hbReader.getPostfixD(hbReader.getDayDiff(s)) + " -> " + (hbReader.getYearDiff(s) + 1) + " " + hbReader.getPostfixY(hbReader.getYearDiff(s) + 1));
                    if (hbReader.getDayDiff(s) < shortest){
                        shortest = hbReader.getDayDiff(s);
                        c = i;
                    }
                }
                i += 1;
            }
        }
        if (!today.isEmpty()){
            if (today.size() == 1) {
                myBot.sendMessageToAdmin(String.format(
                        "****************************\n" +
                                "      Today Happy Birthday to:\n" +
                                "%s\n" +
                                "****************************",
                        today.get(0)
                ));
            }

            else {
                StringBuilder sb = new StringBuilder();
                sb.append("Today Happy Birthdays:\n");
                for (String data : today) sb.append(data).append("\n");
                myBot.sendMessageToAdmin(sb.toString());
            }
        }
        if (!close.isEmpty()){
            if (close.size() == 1) myBot.sendMessageToAdmin("Soon Happy Birthday:\n" + close.get(0));
            else {
                StringBuilder sb = new StringBuilder();
                sb.append("Soon Happy Birthdays:\n");
                for (int j = c; j < close.size(); j++) {
                    sb.append(close.get(j)).append("\n");
                }
                for (int j = 0; j < c; j++) {
                    sb.append(close.get(j)).append("\n");
                }
                myBot.sendMessageToAdmin(sb.toString());
            }
        }
    }
    public static void load(String path, Map<String, ArrayList<String>> hbDtoN, Map<String, ArrayList<String>> hbNtoD, ArrayList<String> datas){
        File file = new File(path);
        if (!file.exists()){
            ColoredMessage.red("HB file not found at \"" + path + "\"", CfgLoader.CompatibilityModeOff);
        }


        try(Scanner scanf = new Scanner(file)) {
            while (scanf.hasNextLine()) {
                String[] arr = scanf.nextLine().split(" ");
                ArrayList<String> temp;
                if (!hbDtoN.containsKey(arr[1])){
                    temp = new ArrayList<>();
                } else {
                    temp = hbDtoN.get(arr[1]);
                }
                temp.add(arr[0]);
                hbDtoN.put(arr[1], temp);
                temp = new ArrayList<>();
                temp.add(arr[1]);
                hbNtoD.put(arr[0], temp);
                if (!datas.contains(arr[1])) datas.add(arr[1]);
            }
            if (datas.isEmpty()){
                ColoredMessage.yellow("HB file is empty or broken", CfgLoader.CompatibilityModeOff);
                System.exit(-1);
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        Comparator<String> monthDayComparator = (s1, s2) -> {
            LocalDate date1 = LocalDate.parse(s1, formatter);
            LocalDate date2 = LocalDate.parse(s2, formatter);

            int result = date1.getMonth().compareTo(date2.getMonth());
            if (result == 0) result = date1.getDayOfMonth() - date2.getDayOfMonth();
            return result;
        };

        datas.sort(monthDayComparator);
    }

    public static int getDayDiff(String srcDate) {
        LocalDate currentDate = LocalDate.now();
        LocalDate sourceLocalDate = LocalDate.parse(srcDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        sourceLocalDate = sourceLocalDate.withYear(currentDate.getYear());
        int ans = (int)ChronoUnit.DAYS.between(currentDate, sourceLocalDate);
        if (ans < 0) return 365 + ans;
        else return ans;
    }

    public static long getYearDiff(String srcDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate currentDate = LocalDate.now();
        LocalDate sourceLocalDate = LocalDate.parse(srcDate, formatter);

        return ChronoUnit.YEARS.between(sourceLocalDate, currentDate);
    }

    public static String getPostfixY(long num){
        if (num % 10 == 1) return "year";
        else if (num % 10 > 0 && num % 10 < 5) return "years";
        else return "years";
    }
    public static String getPostfixD(long num){
        if (num % 10 == 1) return "day";
        else if (num % 10 > 0 && num % 10 < 5) return "days";
        else return "days";
    }
}
