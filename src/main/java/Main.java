import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        String CfgPath = "config.txt";

        MyBot myBot = new MyBot();

        int check = CfgLoader.load(CfgPath);
        switch (check){
            case -1:{
                System.exit(1);
            }
            case -2:{
                ColoredMessage.red("    \"" + CfgLoader.returnable + "\" is not a number");
                System.exit(-2);
            }
            case -3:{
                ColoredMessage.red("    \"" + CfgLoader.returnable + "\" is not a UTC format ( -18 <= UTCtime <= 18)");
                System.exit(-3);
            }
            case -4:{
                ColoredMessage.red("    \"" + CfgLoader.returnable + "\" is not a number");
                System.exit(-4);
            }
        }


        File file = new File(MyBot.languagePath);
        System.out.println("Language file: from \"" + MyBot.languagePath + "\"");
        if (!file.exists()) ColoredMessage.yellow("\n     not loaded. Launching without it.");
        else {
            MyBot.LanguageLoaded = true;
            ColoredMessage.green("    loaded successfully");
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

        if (MyBot.CHATID == 0L) ColoredMessage.green("Bot started");
        else ColoredMessage.green("Messages to " + chatid.substring(0, len/3) + "..." + chatid.substring((len/3)*2 ));
        while (!(input = scan.nextLine()).isEmpty()) myBot.sendMessage(input);

    }
}

