import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;

public class CfgLoader{
    public static void load(String path){
        File file = new File(path);
        try(Scanner scanf = new Scanner(file)){
            while (scanf.hasNextLine()){
                String temp = scanf.nextLine();
                String[] arr;
                if (temp.contains("Token=")){
                    arr = temp.split("\"");
                    MyBot.token = arr[1];
                    continue;
                }
                if (temp.contains("BotName=")){
                    arr = temp.split("\"");
                    MyBot.botName = arr[1];
                    continue;
                }
                if (temp.contains("BasePath=")){
                    arr = temp.split("\"");
                    MyBot.basePath = arr[1];
                    continue;
                }
                if (temp.contains("Separator=")){
                    arr = temp.split("\"");
                    MyBot.sep = arr[1];
                    continue;
                }
                if (temp.contains("ChatID=")){
                    arr = temp.split("\"");
                    MyBot.CHATID = Long.parseLong(arr[1]);
                    continue;
                }
                if (temp.contains("TorrentAuto=")){
                    arr = temp.split("\"");
                    if (Objects.equals(arr[1], "True") || Objects.equals(arr[1], "true"))
                        MyBot.TorrentAutoDownload = true;
                    continue;
                }
                if (temp.contains("AdminNick=")){
                    arr = temp.split("\"");
                    MyBot.Admin = arr[1];
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}
