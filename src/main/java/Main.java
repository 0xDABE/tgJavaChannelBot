import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static long upTimeStart = System.currentTimeMillis();
    public static void main(String[] args){
        run();
    }

    public static void run(){
        String CfgPath = "config.txt";

        String[] requirement = new String[]{
                "Token", "BotName", "BasePath", "LogFileName", "TimeZoneUTCplus"};
        String[] extra = new String[]{
                "ColoredOutput", "ChatID", "AdminNick", "LanguageFileName", "TorrentAuto", "TorrentSavePath",
                "IgnorePortableTorrentClient", "HappyBirthdayFileName", "ShellIsOn"};

        CfgLoader cfgLoader =
                new CfgLoader(CfgPath, requirement, extra);

        if (!cfgLoader.load()){
            System.err.println(cfgLoader.stderr);
            System.exit(-1);
        }

        String lastParam = "";
        try {
            for (String reqParam : requirement) {
                lastParam = reqParam;
                Optional<String> got = Optional.ofNullable(cfgLoader.getCfgValue(reqParam));
                switch (reqParam) {
                    case "Token" -> MyBot.botToken = got.orElse("");
                    case "BotName" -> MyBot.botName = got.orElse("");
                    case "BasePath" -> MyBot.basePath = got.orElse("");
                    case "LogFileName" -> MyBot.LogFileName = got.orElse("");
                    case "TimeZoneUTCplus" -> MyBot.TimeZone = Integer.parseInt(got.orElse("0"));
                }
            }

            for (String extraParam : extra) {
                lastParam = extraParam;
                Optional<String> got = Optional.ofNullable(cfgLoader.getCfgValue(extraParam));
                switch (extraParam) {
                    case "ChatID" -> MyBot.CHATID = Long.parseLong(got.orElse("0"));
                    case "AdminNick" -> MyBot.Admin = got.orElse("");
                    case "LanguageFileName" -> MyBot.languagePath = got.orElse("");
                    case "ShellIsOn" -> MyBot.ShellOn = got.get().equalsIgnoreCase("true");
                    case "ColoredOutput" -> MyBot.coloredOutput = got.get().equalsIgnoreCase("true");
                    case "TorrentAuto" -> MyBot.TorrentAutoDownload = got.get().equalsIgnoreCase("true");
                    case "TorrentSavePath" -> MyBot.TorrentSavePath = got.orElse("");
                    case "IgnorePortableTorrentClient" ->
                            MyBot.IgnorePortableClient = got.get().equalsIgnoreCase("true");
                    case "HappyBirthdayFileName" -> hbReader.HBfileName = got.orElse("");
                }
                if (extraParam.equals("ShellIsOn") || extraParam.equals("ColoredOutput") || extraParam.equals("TorrentAuto"))
                    if (!got.get().equalsIgnoreCase("true") && !got.get().equalsIgnoreCase("false"))
                        ColoredMessage.yellowLn("Parameter \"" + extraParam +
                            "\" is not a True, but also not False (a case does not matter). Running with default value: \"" +
                            extraParam + "=False\"", MyBot.coloredOutput);

            }
        } catch (NumberFormatException ignored){
            System.err.println("Parameter \"" + lastParam + "\" must be a number, but " + 
                    cfgLoader.getCfgValue(lastParam) + " is not a number");
        }

        if (MyBot.TimeZone > 18 || MyBot.TimeZone < -18){
            System.err.println("UTC time zone must equals from 18 to -18");
            System.exit(-1);
        }
        ColoredMessage.yellowLn(cfgLoader.stdwarn, MyBot.coloredOutput);

        MyBot myBot = new MyBot();

        File file = new File(MyBot.languagePath);
        System.out.println("Language file: from \"" + MyBot.languagePath + "\"");
        if (!file.exists()) ColoredMessage.yellow("\n     not loaded. Launching without it.", MyBot.coloredOutput);
        else {
            MyBot.LanguageLoaded = true;
            ColoredMessage.green("    loaded successfully\n", MyBot.coloredOutput);
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

        if (MyBot.CHATID == 0L) ColoredMessage.green("Bot started", MyBot.coloredOutput);
        else ColoredMessage.green("Messages to " + chatid.substring(0, len/3) + "..." +
                chatid.substring((len/3)*2 ) + "\n", MyBot.coloredOutput);


        //todo: switch send messaged to console

        while (true) {
            try {
                while (!(input = scan.nextLine()).isEmpty()) myBot.sendMessageToAdmin(input, true);
            } catch (NoSuchElementException ignored) {
            }
        }
    }
}

