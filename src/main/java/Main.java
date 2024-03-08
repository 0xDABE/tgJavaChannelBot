import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static long upTimeStart = System.currentTimeMillis();
    public static void main(String[] args){
        run();
    }

    public static void run(){
        String CfgPath = "config.txt";

        MyBot myBot = new MyBot();

        int check = CfgLoader.load(CfgPath);
        switch (check){
            case -1:{
                System.exit(1);
            }
            case -2:{
                ColoredMessage.red("    \"" + CfgLoader.returnable + "\" is not a number", CfgLoader.CompatibilityModeOff);
                System.exit(-2);
            }
            case -3:{
                ColoredMessage.red("    \"" + CfgLoader.returnable + "\" is not a UTC format ( -18 <= UTCtime <= 18)", CfgLoader.CompatibilityModeOff);
                System.exit(-3);
            }
            case -4:{
                ColoredMessage.red("    \"" + CfgLoader.returnable + "\" is not a number", CfgLoader.CompatibilityModeOff);
                System.exit(-4);
            }
        }

        File file = new File(MyBot.languagePath);
        System.out.println("Language file: from \"" + MyBot.languagePath + "\"");
        if (!file.exists()) ColoredMessage.yellow("\n     not loaded. Launching without it.", CfgLoader.CompatibilityModeOff);
        else {
            MyBot.LanguageLoaded = true;
            ColoredMessage.green("    loaded successfully\n", CfgLoader.CompatibilityModeOff);
        }

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
        String input, chatid = String.valueOf(MyBot.CHATID);
        int len = chatid.length();

        if (MyBot.CHATID == 0L) ColoredMessage.green("Bot started", CfgLoader.CompatibilityModeOff);
        else ColoredMessage.green("Messages to " + chatid.substring(0, len/3) + "..." +
                chatid.substring((len/3)*2 ) + "\n", CfgLoader.CompatibilityModeOff);


        //todo: switch send messaged to console

        while (true) {
            try {
                while (!(input = scan.nextLine()).isEmpty()) myBot.sendMessageToAdmin(input, true);
            } catch (NoSuchElementException ignored) {
            }
        }
    }
}

