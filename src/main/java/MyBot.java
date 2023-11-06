import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.net.ssl.HttpsURLConnection;
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
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class MyBot extends TelegramLongPollingBot {
    public static long CHATID = 0L;
    public static String token = "";
    public static String botName = "";
    public static String basePath = "";
    public static String LogFileName = "";
    public static int TimeZone = 0;
    public static String sep = "";
    public static String languagePath = "";
    public static String Admin = "";
    public static String TorrentSavePath = "";

    public static boolean TorrentAutoDownload = false;
    public static boolean LanguageLoaded = false;
    public static boolean ShellOn = false;

    public void sendMessage(String in){
        SendMessage sm = new SendMessage();
        if (CHATID == 0L){
            ColoredMessage.yellow("Can't send to empty ChatID");
            return;
        }
        sm.setChatId(CHATID);
        sm.setText(in);
        try{
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();

            if (message.startsWith("/help")) {
                SendMessage m = new SendMessage();
                if (CHATID == 0L) m.setChatId(update.getMessage().getChatId());
                else m.setChatId(CHATID);
                StringBuilder s = new StringBuilder();
                s.append("Available commands:\n\n\n");
                s.append("/help - command list (this)\n\n");
                s.append("/calc -  Calculator\n");
                s.append("Usage: /calc <regex>\n");
                s.append("Example: /calc 12**2 + 1-3\n\n");
                s.append("/shell -  Server shell (not available in channels)\n\n");
                s.append("/tr -  Language translator\n");
                s.append("Usage: /tr - language list\n");
                s.append("/tr <languageSRC>|<languageOUT> <StringToTranslate>\n");
                s.append("/tr <StringToTranslate> automatically translates ru|en or en|ru\n");
                s.append("Example 1: /tr fr|ru merci\n");
                s.append("Example 2: /tr привет\n");
                s.append("Example 3: /tr user");
                m.setText(s.toString());
                try {
                    execute(m);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            if (message.startsWith("/calc")) {
                String cmd = "python calc.py", ans, temp = "";
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
                    SendMessage m = new SendMessage();
                    if (CHATID == 0L) m.setChatId(update.getMessage().getChatId());
                    else m.setChatId(CHATID);
                    while ((ans = br.readLine()) != null) temp = ans;
                    BufferedReader be = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                    ans = temp;
                    if (be.readLine() == null) m.setText(ans);
                    else m.setText("Error");
                    try {
                        execute(m);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            if (message.startsWith("/tr")) {
                boolean DetectFlag = false;
                message = message.replace("/tr", "");
                if (message.isEmpty()){
                    if (!LanguageLoaded){
                        SendMessage sm = new SendMessage();
                        sm.setText("Language file not loaded");
                        if (CHATID == 0L) sm.setChatId(update.getMessage().getChatId());
                        else sm.setChatId(CHATID);
                        try{
                            execute(sm);
                        }catch (TelegramApiException e){
                            e.printStackTrace();
                        }
                        return;
                    }
                    java.io.File file = new java.io.File(languagePath);
                    StringBuilder sb = new StringBuilder();
                    try(Scanner scan = new Scanner(file)){
                        while (scan.hasNextLine()){
                            String temp = scan.nextLine();
                            String[] arr = temp.split("\t");
                            temp = String.format("%-17s" + "%3s", arr[0], arr[1]);
                            sb.append(temp).append("\n");
                        }
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    SendMessage sm = new SendMessage();
                    if (CHATID == 0L) sm.setChatId(update.getMessage().getChatId());
                    else sm.setChatId(CHATID);
                    sm.setText(sb.toString());
                    try {
                        execute(sm);
                    }catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                    return;
                }
                message = message.replaceFirst(" ", "");
                String[] words = message.split(" ");
                String lang;
                StringBuilder sb = new StringBuilder();
                String baseURL = ("https://api.mymemory.translated.net/get");
                if (!words[0].contains("|")){
                    sb.append(words[0]);
                    DetectFlag = true;
                }
                for (int i = 1; i < words.length - 1; i++) sb.append(words[i]).append(" ");
                if (words.length > 1) sb.append(words[words.length - 1]);
                if (DetectFlag){
                    if (langDetect.containsCyrillic(sb.toString())) lang = "ru|en";
                    else{
                        if (langDetect.containsJapanese(sb.toString())) lang = "ja|ru";
                        else lang = "en|ru";
                    }
                }
                else lang = words[0];

                String urlS = decoder.buildUrl(baseURL, sb.toString(), lang);
                try {
                    URL url = new URL(urlS);

                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String[] arr = response.toString().split("\"");
                    String tr = decoder.decodeUnicodeEscape(arr[5]);
                    SendMessage sm = new SendMessage();
                    if (CHATID == 0L) sm.setChatId(update.getMessage().getChatId());
                    else sm.setChatId(CHATID);
                    sm.setText(tr);
                    try{
                        execute(sm);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (message.startsWith("/shell")) {
                if (Objects.equals(Admin, "") || !ShellOn){
                    SendMessage sm = new SendMessage();
                    sm.setChatId(update.getMessage().getChatId());
                    sm.setText("You can't to this");
                    return;
                }
                if (!Objects.equals(update.getMessage().getFrom().getUserName(), Admin)){
                    SendMessage sm = new SendMessage();
                    sm.setChatId(update.getMessage().getChatId());
                    sm.setText("Haha, stupid hacker-huyaker. fak u");
                    try {
                        execute(sm);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                String cmd = "python shell.py", ans;
                message = message.replace("/shell ", "");

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
                    SendMessage m = new SendMessage();
                    if (CHATID == 0L) m.setChatId(update.getMessage().getChatId());
                    else m.setChatId(CHATID);
                    int len = 0;
                    while ((ans = br.readLine()) != null){
                        sb.append(ans);
                        len += ans.length();
                    }
                    if (sb.length() != 0){
                        if (len >= 4096){
                            int startIndex = 0;
                            int endIndex;
                            String messageText = sb.toString();
                            while (startIndex < messageText.length()) {
                                endIndex = Math.min(startIndex + 4096, messageText.length());
                                String part = messageText.substring(startIndex, endIndex);
                                m.setText(part);
                                try {
                                    execute(m);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                                startIndex = endIndex;
                            }
                        }
                        else{
                            m.setText(sb.toString());
                            try {
                                execute(m);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String sender = message.getFrom().getUserName();
            LocalDateTime dateTime = LocalDateTime.now();
            ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fdt = zonedDateTime.format(formatter);
            String mes = update.getMessage().getText();
            mes = mes.replaceAll("\n", " ");
            mes = fdt + " >> " + sender + " >> " + mes + "\n";
            try(FileOutputStream file = new FileOutputStream(basePath + sep + LogFileName, true)){
                file.write(mes.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            List<PhotoSize> photos = update.getMessage().getPhoto();
            String fileId = photos.get(photos.size() - 1).getFileId();
            String sender = update.getMessage().getFrom().getUserName();

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
                    try(FileOutputStream file1 = new FileOutputStream( basePath + sep + LogFileName, true)){
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
            return;
        }
        if (update.hasMessage() && update.getMessage().hasDocument()) {
            Document document = update.getMessage().getDocument();
            String sender = update.getMessage().getFrom().getUserName();

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
                            fileName.charAt(size - 7) == 't' && fileName.charAt(size - 8) == '.' && TorrentAutoDownload){
                        ProcessBuilder pb = new ProcessBuilder("qbittorrent",
                                "--save-path=" + TorrentSavePath + sep,
                                "--add-paused=false",
                                "--sequential",
                                "--skip-dialog=true",
                                basePath + sep + sender + sep + fileName);
                        try {
                            pb.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    LocalDateTime dateTime = LocalDateTime.now();
                    ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String fdt = zonedDateTime.format(formatter);
                    String mes = " SENT A FILE \"" + fileName + "\"";
                    mes = fdt + " ** " + sender + " ** " + mes + "\n";
                    try(FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)){
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
            return;
        }
        if (update.hasMessage() && update.getMessage().hasVoice()) {
            Voice voice = update.getMessage().getVoice();

            String fileId = voice.getFileId();
            String sender = update.getMessage().getFrom().getUserName();

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
                    try(FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)){
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
            return;
        }
        if (update.hasMessage() && update.getMessage().hasVideo()) {
            Video video = update.getMessage().getVideo();

            String fileId = video.getFileId();
            String sender = update.getMessage().getFrom().getUserName();

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
                        try(FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)){
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
            return;
        }

        if (update.hasChannelPost() && update.getChannelPost().hasText()) {
            String message = update.getChannelPost().getText();

            if (message.startsWith("/help")) {
                SendMessage m = new SendMessage();
                if (CHATID == 0L) m.setChatId(update.getChannelPost().getChatId());
                else m.setChatId(CHATID);
                StringBuilder s = new StringBuilder();
                s.append("Available commands:\n\n\n");
                s.append("/help - command list (this)\n\n");
                s.append("/calc -  Calculator\n");
                s.append("Usage: /calc <regex>\n");
                s.append("Example: /calc 12**2 + 1-3\n\n");
                s.append("/shell -  Server shell (bot private message only usage)\n\n");
                s.append("/tr -  Language translator\n");
                s.append("Usage: /tr - language list\n");
                s.append("/tr <languageSRC>|<languageOUT> <StringToTranslate>\n");
                s.append("/tr <StringToTranslate> automatically translates ru|en or en|ru\n");
                s.append("Example 1: /tr fr|ru merci\n");
                s.append("Example 2: /tr привет\n");
                s.append("Example 3: /tr user");
                m.setText(s.toString());
                try {
                    execute(m);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            if (message.startsWith("/calc")) {
                String cmd = "python calc.py", ans, temp = "";
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
                    SendMessage m = new SendMessage();
                    if (CHATID == 0L) m.setChatId(update.getChannelPost().getChatId());
                    else m.setChatId(CHATID);
                    while ((ans = br.readLine()) != null) temp = ans;
                    BufferedReader be = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                    ans = temp;
                    if (be.readLine() == null) m.setText(ans);
                    else m.setText("Error");
                    try {
                        execute(m);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            if (message.startsWith("/tr")) {
                boolean DetectFlag = false;
                message = message.replace("/tr", "");
                if (message.isEmpty()){
                    if (!LanguageLoaded){
                        SendMessage sm = new SendMessage();
                        sm.setText("Language file not loaded");
                        if (CHATID == 0L) sm.setChatId(update.getChannelPost().getChatId());
                        else sm.setChatId(CHATID);
                        try{
                            execute(sm);
                        }catch (TelegramApiException e){
                            e.printStackTrace();
                        }
                        return;
                    }
                    java.io.File file = new java.io.File(languagePath);
                    StringBuilder sb = new StringBuilder();
                    try(Scanner scan = new Scanner(file)){
                        while (scan.hasNextLine()){
                            String temp = scan.nextLine();
                            String[] arr = temp.split("\t");
                            temp = String.format("%-17s" + "%3s", arr[0], arr[1]);
                            sb.append(temp).append("\n");
                        }
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    SendMessage sm = new SendMessage();
                    if (CHATID == 0L) sm.setChatId(update.getChannelPost().getChatId());
                    else sm.setChatId(CHATID);
                    sm.setText(sb.toString());
                    try {
                        execute(sm);
                    }catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                    return;
                }
                message = message.replaceFirst(" ", "");
                String[] words = message.split(" ");
                String lang;
                StringBuilder sb = new StringBuilder();
                String baseURL = ("https://api.mymemory.translated.net/get");
                if (!words[0].contains("|")){
                    sb.append(words[0]);
                    DetectFlag = true;
                }
                for (int i = 1; i < words.length - 1; i++) sb.append(words[i]).append(" ");
                if (words.length > 1) sb.append(words[words.length - 1]);
                if (DetectFlag){
                    if (langDetect.containsCyrillic(sb.toString())) lang = "ru|en";
                    else{
                        if (langDetect.containsJapanese(sb.toString())) lang = "ja|ru";
                        else lang = "en|ru";
                    }
                }
                else lang = words[0];

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
                    SendMessage sm = new SendMessage();
                    if (CHATID == 0L) sm.setChatId(update.getChannelPost().getChatId());
                    else sm.setChatId(CHATID);
                    sm.setText(tr);
                    try{
                        execute(sm);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (message.startsWith("/shell")) {
                SendMessage sm = new SendMessage();
                sm.setChatId(update.getChannelPost().getChatId());
                sm.setText("You can't use shell in channels");
                try {
                    execute(sm);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        if (update.hasChannelPost() && update.getChannelPost().hasText()) {
            Message message = update.getChannelPost();
            String sender = message.getChat().getTitle();
            LocalDateTime dateTime = LocalDateTime.now();
            ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fdt = zonedDateTime.format(formatter);
            String mes = update.getChannelPost().getText();
            mes = mes.replaceAll("\n", " ");
            mes = fdt + " > " + sender + " > " + mes + "\n";
            try(FileOutputStream file = new FileOutputStream(basePath + sep + LogFileName, true)){
                file.write(mes.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if (update.hasChannelPost() && update.getChannelPost().hasPhoto()) {
            List<PhotoSize> photos = update.getChannelPost().getPhoto();
            String fileId = photos.get(photos.size() - 1).getFileId();
            String sender = update.getChannelPost().getChat().getTitle();

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
                    mes = fdt + " * " + sender + " * " + mes + "\n";
                    try(FileOutputStream file1 = new FileOutputStream( basePath + sep + LogFileName, true)){
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
            return;
        }
        if (update.hasChannelPost() && update.getChannelPost().hasDocument()) {
            Document document = update.getChannelPost().getDocument();
            String sender = update.getChannelPost().getChat().getTitle();

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
                            fileName.charAt(size - 7) == 't' && fileName.charAt(size - 8) == '.' && TorrentAutoDownload){
                        ProcessBuilder pb = new ProcessBuilder("qbittorrent",
                                "--save-path=" + TorrentSavePath + sep,
                                "--add-paused=false",
                                "--sequential",
                                "--skip-dialog=true",
                                basePath + sep + sender + sep + fileName);
                        try {
                            pb.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    LocalDateTime dateTime = LocalDateTime.now();
                    ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String fdt = zonedDateTime.format(formatter);
                    String mes = " SENT A FILE \"" + fileName + "\"";
                    mes = fdt + " * " + sender + " * " + mes + "\n";
                    try(FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)){
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
            return;
        }
        if (update.hasChannelPost() && update.getChannelPost().hasVoice()) {
            Voice voice = update.getChannelPost().getVoice();

            String fileId = voice.getFileId();
            String sender = update.getChannelPost().getChat().getTitle();

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
                    mes = formattedDateTime + " * " + sender + " * " + mes + "\n";
                    try(FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)){
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
            return;
        }
        if (update.hasChannelPost() && update.getChannelPost().hasVideo()) {
            Video video = update.getChannelPost().getVideo();

            String fileId = video.getFileId();
            String sender = update.getChannelPost().getChat().getTitle();

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
                        mes = fdt + " * " + sender + " * " + mes + "\n";
                        try(FileOutputStream file1 = new FileOutputStream(basePath + sep + LogFileName, true)){
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
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
