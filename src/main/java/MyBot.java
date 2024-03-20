import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class MyBot extends TelegramLongPollingBot {
    public static long CHATID = 0L;
    public static String token = "";
    public static String botName = "";
    public static String basePath = "";
    public static String LogFileName = "";
    public static String sep = "";
    public static String languagePath = "";
    public static String Admin = "";
    public static String TorrentSavePath = "";

    public static int TimeZone = 0;

    public static ArrayList<String> trustedUsersFromConfig = new ArrayList<>();

    public static boolean IgnorePortableClient = false;
    public static boolean TorrentAutoDownload = false;
    public static boolean LanguageLoaded = false;
    public static boolean ShellOn = false;

    public enum User{
        Admin, Trusted, User;

        public String toString(){
            switch (this){
                case Admin -> {return "Admin";}
                case Trusted -> {return "Trusted";}
            }
            return "User";
        }
    }

    public void sendMessageToAdmin(String in, boolean makrdown) {
        SendMessage sm = new SendMessage();
        if (CHATID == 0L) {
            ColoredMessage.yellow("Can't send to empty ChatID", CfgLoader.CompatibilityModeOff);
            return;
        }
        sm.enableMarkdownV2(makrdown);
        sm.setChatId(CHATID);
        sm.setText(in);
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String in, long CHATID, boolean makrdown) {
        SendMessage sm = new SendMessage();
        if (CHATID == 0L) {
            ColoredMessage.yellow("Can't send to empty ChatID", CfgLoader.CompatibilityModeOff);
            return;
        }
        sm.setChatId(CHATID);
        sm.enableMarkdownV2(makrdown);
        sm.setText(in);
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void helpUser(Message messageFromUpd) {
        String s = """
                Available commands:


                    /help - command list (this)
                    
                    /whoami - your current previlegies
                    Admin - root user (always trusted, solo)
                    Trusted - trusted user (has more power than common user)
                    User - common user
    
                    /calc -  Calculator
                    Usage: /calc <regex>
                    Example: /calc 12**2 + 1-3
    
                    /shell -  Server shell (not available in channels)
    
                    /tr -  Language translator
                    Usage: /tr - language list
                    /tr <languageSRC>|<languageOUT> <StringToTranslate>
                    /tr <StringToTranslate> automatically translates ru|en or en|ru
                    Example 1: /tr fr|ru merci
                    Example 2: /tr привет
                    Example 3: /tr user
                    
                Features:
                    1. Trusted users are able to:
                      - auto download raw magnet-links ("magnet:<...>")
                      - auto download .torent files""";

        sendMessage(s, messageFromUpd.getChatId(), false);
    }

    public void helpChannel(Message messageFromUpd) {
        String s = """
                Available commands:


                    /help - command list (this)
                    
                    /whoami - your current previlegies
                    Admin - root user (always trusted, solo)
                    Trusted - trusted user (has more power than common user)
                    User - common user
    
                    /calc -  Calculator
                    Usage: /calc <regex>
                    Example: /calc 12**2 + 1-3
    
                    /shell -  Server shell (bot private message only usage)
    
                    /tr -  Language translator
                    Usage: /tr - language list
                    /tr <languageSRC>|<languageOUT> <StringToTranslate>
                    /tr <StringToTranslate> automatically translates ru|en or en|ru
                    Example 1: /tr fr|ru merci
                    Example 2: /tr привет
                    Example 3: /tr user
                    
                Features:
                    1. Trusted users are able to:
                      - auto download raw magnet-links ("magnet:<...>")
                      - auto download .torent files""";



        sendMessage(s, messageFromUpd.getChatId(), false);
    }

    public void happyBirthday(Message message) {
        String text = message.getText();
        if (!message.getFrom().getUserName().equals(Admin)){
            sendMessage("You can't sorry", message.getChatId(), false);
            return;
        }
        if (text.equals("/hb")) {
            hbReader.get(20, this);
            return;
        }
        if (text.equals("/hb all") || text.equals("/hb a")) {
            hbReader.get(366, this);
            return;
        }
        text = text.replace("/hb ", "");
        try {
            int num = Integer.parseInt(text);
            hbReader.get(num, this);
        } catch (NumberFormatException e) {
            sendMessageToAdmin("\"" + text + "\" is not a number", false);
        }
    }

    public void calcPy(Message messageFromUpd) {
        String cmd = "python calc.py", ans, temp = "", message = messageFromUpd.getText();
        message = message.replace("/calc ", "");

        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec(cmd);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
            bw.write(message);
            bw.newLine();
            bw.flush();
            bw.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String m;
            while ((ans = br.readLine()) != null) temp = ans;
            BufferedReader be = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            ans = temp;
            if (be.readLine() == null) m = ans;
            else m = "Error";
            sendMessage(m, messageFromUpd.getChatId(), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void translate(Message messageFromUpd) {
        boolean DetectFlag = false;
        String message = messageFromUpd.getText().replace("/tr", "");
        if (message.isEmpty()) {
            if (!LanguageLoaded) {
                sendMessage("Language file not loaded", messageFromUpd.getChatId(), false);
                return;
            }
            java.io.File file = new java.io.File(languagePath);
            StringBuilder sb = new StringBuilder();
            try (Scanner scan = new Scanner(file)) {
                while (scan.hasNextLine()) {
                    String temp = scan.nextLine();
                    String[] arr = temp.split("\t");
                    temp = String.format("%-17s" + "%3s", arr[0], arr[1]);
                    sb.append(temp).append("\n");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            sendMessage(sb.toString(), messageFromUpd.getChatId(), false);
            return;
        }
        message = message.replaceFirst(" ", "");
        String[] words = message.split(" ");
        String lang;
        StringBuilder sb = new StringBuilder();
        String baseURL = ("https://api.mymemory.translated.net/get");
        if (!words[0].contains("|")) {
            sb.append(words[0]);
            DetectFlag = true;
        }
        for (int i = 1; i < words.length - 1; i++) sb.append(words[i]).append(" ");
        if (words.length > 1) sb.append(words[words.length - 1]);
        if (DetectFlag) {
            if (langDetect.containsCyrillic(sb.toString())) lang = "ru|en";
            else {
                if (langDetect.containsJapanese(sb.toString())) lang = "ja|ru";
                else lang = "en|ru";
            }
        } else lang = words[0];

        String urlS = decoder.buildUrl(baseURL, sb.toString(), lang);
        try {
            URL url = new URL(urlS);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            String[] arr = response.toString().split("\"");
            String tr = decoder.decodeUnicodeEscape(arr[5]);

            sendMessage(tr, messageFromUpd.getChatId(), false);
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shellPy(Message messageFromUpd) {
        String message = messageFromUpd.getText();
        if (Objects.equals(Admin, "") || !ShellOn) {
            sendMessage("You can't to this", messageFromUpd.getChatId(), false);
            return;
        }
        if (!Objects.equals(messageFromUpd.getFrom().getUserName(), Admin)) {
            sendMessage("Haha, stupid hacker-huyaker. fak u", messageFromUpd.getChatId(), false);
            return;
        }
        String cmd = "python shell.py", ans;
        message = message.replace("/shell ", "");

        if (message.equalsIgnoreCase("stop")) System.exit(0);

        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec(cmd);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
            bw.write(message);
            bw.newLine();
            bw.flush();
            bw.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream(), Charset.forName("cp866")));
            StringBuilder sb = new StringBuilder();
            String m;
            int len = 0;
            while ((ans = br.readLine()) != null) {
                sb.append(ans);
                len += ans.length();
            }
            if (!sb.isEmpty()) {
                if (len >= 4096) {
                    int startIndex = 0;
                    int endIndex;
                    String messageText = sb.toString();
                    while (startIndex < messageText.length()) {
                        endIndex = Math.min(startIndex + 4096, messageText.length());
                        m = messageText.substring(startIndex, endIndex);
                        sendMessage(m, messageFromUpd.getChatId(), false);
                        startIndex = endIndex;
                    }
                } else sendMessage(sb.toString(), messageFromUpd.getChatId(), false);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeToLogFile(Message message) {
        String sender = message.getFrom().getUserName();
        LocalDateTime dateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fdt = zonedDateTime.format(formatter);
        String mes = message.getText();
        mes = mes.replaceAll("\n", " ");
        mes = fdt + " >> " + sender + " >> " + mes + "\n";
        try (FileOutputStream file = new FileOutputStream(basePath + sep + LogFileName, true)) {
            file.write(mes.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void savePhoto(Message message) {
        List<PhotoSize> photos = message.getPhoto();
        String fileId = photos.get(photos.size() - 1).getFileId();
        String sender = message.getFrom().getUserName();

        GetFile getFileRequest = new GetFile();
        getFileRequest.setFileId(fileId);

        try {
            File file = execute(getFileRequest);
            String filePath = file.getFilePath();

            java.io.File downloadedFile = downloadFile(filePath);

            java.nio.file.Path sourcePath = downloadedFile.toPath();
            java.nio.file.Path targetPath = Paths.get(basePath + sep + sender + sep + downloadedFile.getName() + ".png");

            try {
                Files.createDirectories(targetPath.getParent());
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                LocalDateTime dateTime = LocalDateTime.now();
                ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String fdt = zonedDateTime.format(formatter);
                String mes = " SENT A PHOTO ";
                mes = fdt + " ** " + sender + " ** " + mes + "\n";
                try (FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)) {
                    file1.write(mes.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void saveDoc(Message message) {
        Document document = message.getDocument();
        String sender = message.getFrom().getUserName();

        String fileId = document.getFileId();

        GetFile getFileRequest = new GetFile();
        getFileRequest.setFileId(fileId);

        try {
            File file = execute(getFileRequest);
            String filePath = file.getFilePath();

            java.io.File downloadedFile = downloadFile(filePath);
            String fileName = document.getFileName();
            int size = fileName.length();

            java.nio.file.Path sourcePath = downloadedFile.toPath();
            java.nio.file.Path targetPath = Paths.get(basePath + sep + sender + sep + fileName);

            try {
                Files.createDirectories(targetPath.getParent());
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                if (fileName.charAt(size - 1) == 't' && fileName.charAt(size - 2) == 'n' &&
                        fileName.charAt(size - 3) == 'e' && fileName.charAt(size - 4) == 'r' &&
                        fileName.charAt(size - 5) == 'r' && fileName.charAt(size - 6) == 'o' &&
                        fileName.charAt(size - 7) == 't' && fileName.charAt(size - 8) == '.' &&
                        TorrentAutoDownload && isTrusted(message.getFrom().getUserName())) {
                    ProcessBuilder pb = getProcessBuilder(sender, fileName);
                    try {
                        pb.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    sendMessage("You are not trusted user. Beg help from admin, little pussy",
                            message.getChatId(), false);
                    return;
                }
                LocalDateTime dateTime = LocalDateTime.now();
                ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String fdt = zonedDateTime.format(formatter);
                String mes = " SENT A FILE \"" + fileName + "\"";
                mes = fdt + " ** " + sender + " ** " + mes + "\n";
                try (FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)) {
                    file1.write(mes.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static ProcessBuilder getProcessBuilder(String sender, String fileName) {
        String TorrentClientExecPath;
        if (IgnorePortableClient) TorrentClientExecPath = "qbittorrent";
        else TorrentClientExecPath = "qbittorrentPorted";
        return new ProcessBuilder(TorrentClientExecPath,
                "--save-path=" + TorrentSavePath + sep,
                "--add-paused=false",
                "--sequential",
                "--skip-dialog=true",
                basePath + sep + sender + sep + fileName);
    }

    public void saveVoice(Message message) {
        Voice voice = message.getVoice();

        String fileId = voice.getFileId();
        String sender = message.getFrom().getUserName();

        GetFile getFileRequest = new GetFile();
        getFileRequest.setFileId(fileId);

        try {
            File file = execute(getFileRequest);
            String filePath = file.getFilePath();

            java.io.File downloadedFile = downloadFile(filePath);

            java.nio.file.Path sourcePath = downloadedFile.toPath();
            java.nio.file.Path targetPath = Paths.get(basePath + sep + sender + sep + voice.getFileId() + "_" + voice.getDuration() + ".mp3");

            try {
                Files.createDirectories(targetPath.getParent());
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                LocalDateTime dateTime = LocalDateTime.now();
                ZoneOffset zoneOffset = ZoneOffset.ofHours(TimeZone);
                ZonedDateTime zonedDateTime = dateTime.atZone(zoneOffset);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = zonedDateTime.format(formatter);
                String mes = " SENT A VOICE ";
                mes = formattedDateTime + " ** " + sender + " ** " + mes + "\n";
                try (FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)) {
                    file1.write(mes.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void saveVideoBestQual(Message message) {
        Video video = message.getVideo();

        String fileId = video.getFileId();
        String sender = message.getFrom().getUserName();

        GetFile getFileRequest = new GetFile();
        getFileRequest.setFileId(fileId);

        try {
            File file = execute(getFileRequest);
            long fileSize = file.getFileSize();

            if (fileSize > 0) {
                java.io.File downloadedFile = downloadFile(file.getFilePath());

                java.nio.file.Path sourcePath = downloadedFile.toPath();
                java.nio.file.Path targetPath = Paths.get(basePath + sep + sender + sep + video.getFileId() + "_" + video.getDuration() + ".mp4");

                try {
                    Files.createDirectories(targetPath.getParent());
                    Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    LocalDateTime dateTime = LocalDateTime.now();
                    ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String fdt = zonedDateTime.format(formatter);
                    String mes = " SENT A VIDEO ";
                    mes = fdt + " ** " + sender + " ** " + mes + "\n";
                    try (FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)) {
                        file1.write(mes.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void runWithMagnetLink(String link){
        String TorrentClientExecPath;
        if (IgnorePortableClient) TorrentClientExecPath = "qbittorrent";
        else TorrentClientExecPath = "qbittorrentPorted";
        ProcessBuilder pb = new ProcessBuilder(TorrentClientExecPath,
                "--save-path=" + TorrentSavePath + sep,
                "--add-paused=false",
                "--sequential",
                "--skip-dialog=true",
                link);
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isTrusted(String user){
        if (user.equals(MyBot.Admin)) return true;
        for (String item : trustedUsersFromConfig) if (user.equals(item)) return true;
        return false;
    }

    public User getUserType(String user){
        if (user.equals(MyBot.Admin)) return User.Admin;
        if (isTrusted(user)) return User.Trusted;
        return User.User;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();

            if (message.startsWith("/help")) {
                writeToLogFile(update.getMessage());
                helpUser(update.getMessage());
                return;
            }
            if (message.startsWith("/hb")) {
                writeToLogFile(update.getMessage());
                happyBirthday(update.getMessage());
                return;
            }
            if (message.startsWith("/calc")) {
                writeToLogFile(update.getMessage());
                calcPy(update.getMessage());
                return;
            }
            if (message.startsWith("/tr")) {
                writeToLogFile(update.getMessage());
                translate(update.getMessage());
                return;
            }
            if (message.startsWith("/shell")) {
                writeToLogFile(update.getMessage());
                shellPy(update.getMessage());
                return;
            }
            if (message.startsWith("/addt")) {
                writeToLogFile(update.getMessage());
                addTrustedUser(update.getMessage());
                return;
            }
            if (message.startsWith("/remt")) {
                writeToLogFile(update.getMessage());
                removeTrustedUser(update.getMessage());
                return;
            }
            if (message.startsWith("/gett")) {
                writeToLogFile(update.getMessage());
                getTrustedUsers(update.getMessage());
                return;
            }
            if (message.equals("/whoami")) {
                writeToLogFile(update.getMessage());
                whoAmI(update.getMessage());
                return;
            }
            if (message.startsWith("magnet:")){
                writeToLogFile(update.getMessage());
                if (isTrusted(update.getMessage().getFrom().getUserName()))
                    runWithMagnetLink(message);
                else {
                    sendMessage("You are not trusted user. Beg help from admin, little pussy",
                            update.getMessage().getChatId(), false);
                    return;
                }
                return;
            }
            if (message.equals("/test")){
                writeToLogFile(update.getMessage());
                runTest(update.getMessage());
                return;
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            writeToLogFile(update.getMessage());
            return;
        }

        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            savePhoto(update.getMessage());
            return;
        }

        if (update.hasMessage() && update.getMessage().hasDocument()) {
            saveDoc(update.getMessage());
            return;
        }

        if (update.hasMessage() && update.getMessage().hasVoice()) {
            saveVoice(update.getMessage());
            return;
        }

        if (update.hasMessage() && update.getMessage().hasVideo()) {
            saveVideoBestQual(update.getMessage());
            return;
        }

        if (update.hasChannelPost() && update.getChannelPost().hasText()) {
            String message = update.getChannelPost().getText();

            if (message.startsWith("/help")) {
                helpChannel(update.getChannelPost());
                return;
            }
            if (message.startsWith("/calc")) {
                calcPy(update.getChannelPost());
                return;
            }
            if (message.startsWith("/tr")) {
                translate(update.getChannelPost());
                return;
            }
            if (message.startsWith("/shell")) {
                sendMessage("You can't use shell in channels", update.getChannelPost().getChatId(), false);
                return;
            }
        }

        if (update.hasChannelPost() && update.getChannelPost().hasText()) {
            writeToLogFile(update.getChannelPost());
            return;
        }

        if (update.hasChannelPost() && update.getChannelPost().hasPhoto()) {
            savePhoto(update.getChannelPost());
            return;
        }

        if (update.hasChannelPost() && update.getChannelPost().hasDocument()) {
            saveDoc(update.getChannelPost());
            return;
        }

        if (update.hasChannelPost() && update.getChannelPost().hasVoice()) {
            saveVoice(update.getChannelPost());
            return;
        }

        if (update.hasChannelPost() && update.getChannelPost().hasVideo()) {
            saveVideoBestQual(update.getChannelPost());
            return;
        }
    }

    public static void addTrustedUser(String user){
        if (!trustedUsersFromConfig.contains(user)) trustedUsersFromConfig.add(user);
    }

    public static void removeTrustedUser(String user){
        trustedUsersFromConfig.remove(user);
    }

    public void addTrustedUser(Message message){
        if (message.getFrom().getUserName().equals(Admin))
            addTrustedUser(message.getText().
                    replace("/addt ", "").replace("@", "").trim());
        else sendMessage("You can't little pussy", message.getChatId(), false);
    }

    public void removeTrustedUser(Message message){
        if (message.getFrom().getUserName().equals(Admin))
            removeTrustedUser(message.getText().
                    replace("/remt ", "").replace("@", "").trim());
        else sendMessage("You can't little pussy", message.getChatId(), false);
    }

    public void getTrustedUsers(Message message){
        if (isTrusted(message.getFrom().getUserName())) {
            StringBuilder sb = new StringBuilder();
            for (String item : trustedUsersFromConfig) sb.append("@").append(item).append(", ");
            sb.delete(sb.length() - 2, sb.length() - 1);
            sendMessage(sb.toString(), message.getChatId(), false);
        }
        else sendMessage("You are not trusted user to get trusted users lol", message.getChatId(), false);
    }

    public void whoAmI(Message message){
        sendMessage("You are \"" + getUserType(message.getFrom().getUserName()).toString() + "\"",
                message.getChatId(), false);
    }

    public void runTest(Message message){
        int errors = 0, warnings = 0;
        if (!message.getFrom().getUserName().equals(Admin)){
            sendMessage("You are too stupid to do test stuff, only admin can",
                    message.getChatId(), false);
            return;
        }

        String ok = "\uD83D\uDC4D", err = "\uD83D\uDC80";

        SendMessage sm = new SendMessage();
        sm.enableMarkdownV2(true);
        sm.setChatId(CHATID); // emojis:  👍⚠💀

        StringBuilder sb = new StringBuilder();

        {                                           //  system block
            sb.append("```system").append("\n");
            sb.append("Token: ").append(ok).append("\n");        //      if u see this message, token is always ok

            sb.append("Shell: ");                   
            if (ShellOn) sb.append("ON");
            else sb.append("OFF");
            sb.append("\n");

            sb.append("```").append("\n");
        }

        {                                           // trusted users block
            sb.append("Current trusted users: ").append(trustedUsersFromConfig.size()).append("\n");
            for (String user : trustedUsersFromConfig) sb.append("    @").append(user).append("\n");
        }

        {                                           // torrents block
            sb.append("```Torrents").append("\n").append("Auto : ");

            if (TorrentAutoDownload) {
                sb.append("ON").append("\n");
                java.io.File file = new java.io.File(TorrentSavePath);
                sb.append("Path: ");
                if (file.exists()) sb.append(ok).append("\n");
                else{
                    sb.append(err).append(" (path unaccessible)\n");
                    errors+=1;
                }
                sb.append("```").append("\n");
            }
            else sb.append("OFF").append("\n").append("```").append("\n");
        }


        sb.append("Uptime: ").
                append(Times.getTimeMillis(System.currentTimeMillis() - Main.upTimeStart).replace(".", ",")).
                append("\n");

        sb.append("\n").append("Warnings: ").append(warnings).append("\n")
                .append("Errors: ").append(errors).append("\n");

        sendMessageToAdmin(sb.toString(), true);
    }

    //todo: add command handler to log


    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
