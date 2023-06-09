import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        MyBot myBot = new MyBot();
        CfgLoader.load("config.txt");
        MyBot.languagePath = "lan.txt";

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
        String input;
        while (!(input = scan.nextLine()).isEmpty()) myBot.sendMessage(input);

        System.exit(0);
    }
}

