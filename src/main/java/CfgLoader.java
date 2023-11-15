import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public class CfgLoader{
    public static String returnable = "";
    public static int load(String path){
        File file = new File(path);
        if (!file.exists()){
            ColoredMessage.red("Config not found at \"" + path + "\"");
            return -1;
        }
        System.out.println("Config: from \"" + path + "\"");
        try(Scanner scanf = new Scanner(file)){
            while (scanf.hasNextLine()){
                String temp = scanf.nextLine();
                String[] arr;
                if (temp.contains("Token=")){
                    arr = temp.split("\"");
                    MyBot.token = arr[1];
                    if (Objects.equals(MyBot.token, "")){
                        ColoredMessage.red("    Error: token is empty");
                        return -1;
                    }
                    continue;
                }
                if (temp.contains("BotName=")){
                    arr = temp.split("\"");
                    MyBot.botName = arr[1];
                    if (Objects.equals(MyBot.botName, "")){
                        ColoredMessage.red("    Error: BotName is empty");
                        return -1;
                    }
                    continue;
                }
                if (temp.contains("DoNotUsePortableTorrentClient=")){
                    arr = temp.split("\"");
                    if (Objects.equals(arr[1].toLowerCase(Locale.ROOT), "true"))
                        MyBot.NotPortableClient = true;
                    else if (!arr[1].toLowerCase(Locale.ROOT).equals("false")) ColoredMessage.yellow("    DoNotUsePortableTorrentClient is not True, but also not a False. " +
                            "Launching with DoNotUsePortableTorrentClient=\"False\" (case does not matter)");
                    continue;
                }
                if (temp.contains("BasePath=")){
                    arr = temp.split("\"");
                    MyBot.basePath = arr[1];
                    if (Objects.equals(MyBot.basePath, "")){
                        ColoredMessage.red("    Error: BasePath is empty");
                        return -1;
                    }
                    Paths.get(MyBot.basePath).toFile().mkdirs();
                    continue;
                }
                if (temp.contains("Separator=")){
                    arr = temp.split("\"");
                    MyBot.sep = arr[1];
                    if (Objects.equals(MyBot.sep, "")){
                        ColoredMessage.red("    Error: Separator is empty");
                        return -1;
                    }
                    continue;
                }
                if (temp.contains("ChatID=")){
                    arr = temp.split("\"");
                    try{
                        if (Objects.equals(arr[1], "")) MyBot.CHATID = 0L;
                        else MyBot.CHATID = Long.parseLong(arr[1]);
                    }
                    catch (NumberFormatException e){
                        returnable = arr[1];
                        return -4;
                    }
                    continue;
                }
                if (temp.contains("TorrentAuto=")){
                    arr = temp.split("\"");
                    if (Objects.equals(arr[1].toLowerCase(Locale.ROOT), "true"))
                        MyBot.TorrentAutoDownload = true;
                    else if (!arr[1].toLowerCase(Locale.ROOT).equals("false")) ColoredMessage.yellow("    TorrentAuto is not True, but also not a False. " +
                            "Launching with TorrentAuto=\"False\" (case does not matter)");
                    continue;
                }
                if (temp.contains("AdminNick=")){
                    arr = temp.split("\"");
                    MyBot.Admin = arr[1];
                    continue;
                }
                if (temp.contains("LogFileName=")){
                    arr = temp.split("\"");
                    MyBot.LogFileName = arr[1];
                    if (Objects.equals(MyBot.LogFileName, "")){
                        ColoredMessage.red("    Error: LogFileName is empty");
                        return -1;
                    }
                }
                if (temp.contains("LanguageFileName=")){
                    arr = temp.split("\"");
                    MyBot.languagePath = arr[1];
                    if (Objects.equals(MyBot.languagePath, "")){
                        ColoredMessage.yellow("    LanguageFileName is empty. Launching without it");
                    }
                }
                if (temp.contains("HappyBirthdayFileName=")){
                    arr = temp.split("\"");
                    hbReader.HBfileName = arr[1];
                    if (Objects.equals(hbReader.HBfileName, "happy.txt")){
                        ColoredMessage.yellow("    HappyBirthdayFileName is empty. Launching without with \"happy.txt\"");
                    }
                }
                if (temp.contains("TorrentSavePath=")){
                    arr = temp.split("\"");
                    MyBot.TorrentSavePath = arr[1];
                    if (Objects.equals(MyBot.TorrentSavePath, "")){
                        ColoredMessage.yellow("    TorrentSavePath is empty. Launching without auto torrent downloading");
                    }
                }
                if (temp.contains("ShellIsOn=")){
                    arr = temp.split("\"");
                    if (Objects.equals(arr[1].toLowerCase(Locale.ROOT), "true"))
                        MyBot.ShellOn = true;
                    else if (!arr[1].toLowerCase(Locale.ROOT).equals("false")) ColoredMessage.yellow("    ShellIsOn is not True, but also not a False. " +
                            "Launching with ShellIsOn=\"False\" (case does not matter)");
                    continue;
                }
                if (temp.contains("TimeZoneUTCplus=")){
                    arr = temp.split("\"");
                    try{
                        MyBot.TimeZone = Integer.parseInt(arr[1]);
                    }
                    catch (NumberFormatException e){
                        returnable = arr[1];
                        return -2;
                    }

                    if (MyBot.TimeZone < -18 || MyBot.TimeZone > 18){
                        returnable = arr[1];
                        return -3;
                    }

                    if (MyBot.TimeZone == 0){
                        ColoredMessage.red("    Error: TimeZoneUTCplus is empty");
                        return -1;
                    }
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        if (Objects.equals(MyBot.Admin, "") && MyBot.ShellOn){
            ColoredMessage.yellow("    You set ShellIsOn to \"true\", but " +
                    "not set Admin nick in config.");
            ColoredMessage.yellow("    Launching with ShellIsOn=\"False\"\n");
        }
        if (Objects.equals(MyBot.TorrentSavePath, "") && MyBot.TorrentAutoDownload){
            ColoredMessage.yellow("    You set TorrentAuto to \"true\", but " +
                    "not set torrent downloading path in config.");
            ColoredMessage.yellow("    Launching with TorrentAuto=\"False\"\n");
        }
        if (Objects.equals(MyBot.sep, "") || Objects.equals(MyBot.basePath, "")
                || Objects.equals(MyBot.botName, "") || Objects.equals(MyBot.token, "")){
            ColoredMessage.red("Config is not correct");
            return -1;
        }
        else ColoredMessage.green("    loaded successfully");
        return 0;
    }
}
