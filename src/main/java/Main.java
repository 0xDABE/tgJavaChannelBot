import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Scanner;

class hiddenData{
    public static String token = ""; // bot's token
    public static String botName = ""; // bot's nickname
    public static String basePath = ""; // head-base folder. There will be created log-file and sender's data in different folders
    public static long chatID = 0; // channel's id (NOT A GROUP) not easy to get it. Long value is 123L
    public static String separator = ""; // your OS separator   if linux "\"     if windows "/"
}

public class Main {
    public static void main(String[] args) {
        MyBot myBot = new MyBot();

        Thread botThread = new Thread(() -> {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(myBot);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });

        botThread.start();

        Scanner scan = new Scanner(System.in);
        String input = "0";
        do{
            input = scan.nextLine();
            myBot.sendMes(input);
        }while (true);

    }
}

