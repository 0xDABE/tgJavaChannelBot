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

    public static boolean NotPortableClient = false;
    public static boolean TorrentAutoDownload = false;
    public static boolean LanguageLoaded = false;
    public static boolean ShellOn = false;

    public void sendMessage(String in) {
        SendMessage sm = new SendMessage();
        if (CHATID == 0L) {
            ColoredMessage.yellow("Can't send to empty ChatID");
            return;
        }
        sm.setChatId(CHATID);
        sm.setText(in);
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private void helpUser(Message messageFromUpd) {
        SendMessage m = new SendMessage();
        if (CHATID == 0L) m.setChatId(messageFromUpd.getChatId());
        else m.setChatId(CHATID);
        String s = """
                Available commands:


                /help - command list (this)

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
                Example 3: /tr user""";
        m.setText(s);
        try {
            execute(m);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void helpChannel(Message messageFromUpd) {
        SendMessage m = new SendMessage();
        if (CHATID == 0L) m.setChatId(messageFromUpd.getChatId());
        else m.setChatId(CHATID);
        String s = """
                Available commands:


                /help - command list (this)

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
                Example 3: /tr user""";
        m.setText(s);
        try {
            execute(m);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void happyBirthday(String message) {
        if (message.equals("/hb")) {
            hbReader.get(20, this);
            return;
        }
        if (message.equals("/hb all") || message.equals("/hb a")) {
            hbReader.get(366, this);
            return;
        }
        message = message.replace("/hb ", "");
        try {
            int num = Integer.parseInt(message);
            hbReader.get(num, this);
        } catch (NumberFormatException e) {
            SendMessage sm = new SendMessage();
            sm.setText("\"" + message + "\" is not a number");
            sm.setChatId(CHATID);
            try {
                execute(sm);
            } catch (TelegramApiException e2) {
                e2.printStackTrace();
            }
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
            SendMessage m = new SendMessage();
            if (CHATID == 0L) m.setChatId(messageFromUpd.getChatId());
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
    }

    public void translate(Message messageFromUpd) {
        boolean DetectFlag = false;
        String message = messageFromUpd.getText().replace("/tr", "");
        if (message.isEmpty()) {
            if (!LanguageLoaded) {
                SendMessage sm = new SendMessage();
                sm.setText("Language file not loaded");
                if (CHATID == 0L) sm.setChatId(messageFromUpd.getChatId());
                else sm.setChatId(CHATID);
                try {
                    execute(sm);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
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
            SendMessage sm = new SendMessage();
            if (CHATID == 0L) sm.setChatId(messageFromUpd.getChatId());
            else sm.setChatId(CHATID);
            sm.setText(sb.toString());
            try {
                execute(sm);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
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
            SendMessage sm = new SendMessage();
            if (CHATID == 0L) sm.setChatId(messageFromUpd.getChatId());
            else sm.setChatId(CHATID);
            sm.setText(tr);
            try {
                execute(sm);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shellPy(Message messageFromUpd) {
        String message = messageFromUpd.getText();
        if (Objects.equals(Admin, "") || !ShellOn) {
            SendMessage sm = new SendMessage();
            sm.setChatId(messageFromUpd.getChatId());
            sm.setText("You can't to this");
            try {
                execute(sm);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        if (!Objects.equals(messageFromUpd.getFrom().getUserName(), Admin)) {
            SendMessage sm = new SendMessage();
            sm.setChatId(messageFromUpd.getChatId());
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
            SendMessage m = new SendMessage();
            if (CHATID == 0L) m.setChatId(messageFromUpd.getChatId());
            else m.setChatId(CHATID);
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
                        String part = messageText.substring(startIndex, endIndex);
                        m.setText(part);
                        try {
                            execute(m);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        startIndex = endIndex;
                    }
                } else {
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
                        fileName.charAt(size - 7) == 't' && fileName.charAt(size - 8) == '.' && TorrentAutoDownload) {
                    String TorrentClientExecPath;
                    if (NotPortableClient) TorrentClientExecPath = "qbittorrent";
                    else TorrentClientExecPath = "qbittorrentPorted";
                    ProcessBuilder pb = new ProcessBuilder(TorrentClientExecPath,
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

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();

            if (message.startsWith("/help")) {
                helpUser(update.getMessage());
                return;
            }
            if (message.startsWith("/hb")) {
                happyBirthday(message);
                return;
            }
            if (message.startsWith("/calc")) {
                calcPy(update.getMessage());
                return;
            }
            if (message.startsWith("/tr")) {
                translate(update.getMessage());
                return;
            }
            if (message.startsWith("/shell")) {
                shellPy(update.getMessage());
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

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
