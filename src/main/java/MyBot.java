import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;



public class MyBot extends TelegramLongPollingBot {
    public long CHATID = hiddenData.chatID;
    public String token = hiddenData.token;
    public String botName = hiddenData.botName;
    public String basePath = hiddenData.basePath;
    public String sep = hiddenData.separator;

    public void sendMes(String in){
        SendMessage sm = new SendMessage();
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
            Message message = update.getMessage();
            String sender = message.getFrom().getUserName();
            LocalDateTime dateTime = LocalDateTime.now();
            ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fdt = zonedDateTime.format(formatter);
            String mes = update.getMessage().getText();
            mes = mes.replaceAll("\n", " ");
            mes = fdt + " >> " + sender + " >> " + mes + "\n";
            try(FileOutputStream file = new FileOutputStream(basePath + sep + "chatLog.txt", true)){
                file.write(mes.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(sender + " SENT A MESSAGE");
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
                    try(FileOutputStream file1 = new FileOutputStream( basePath + sep + "chatLog.txt", true)){
                        file1.write(mes.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(sender + " SENT A PHOTO");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
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

                java.nio.file.Path sourcePath = downloadedFile.toPath();
                java.nio.file.Path targetPath = Paths.get(basePath + sep + sender + sep + document.getFileName());

                try {
                    Files.createDirectories(targetPath.getParent());
                    Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    LocalDateTime dateTime = LocalDateTime.now();
                    ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String fdt = zonedDateTime.format(formatter);
                    String mes = " SENT A FILE \"" + document.getFileName() + "\"";
                    mes = fdt + " ** " + sender + " ** " + mes + "\n";
                    try(FileOutputStream file1 = new FileOutputStream(basePath + sep + "chatLog.txt", true)){
                        file1.write(mes.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(sender + " SENT A FILE \"" + document.getFileName() + "\"");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
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
                    ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String fdt = zonedDateTime.format(formatter);
                    String mes = " SENT A VOICE ";
                    mes = fdt + " ** " + sender + " ** " + mes + "\n";
                    try(FileOutputStream file1 = new FileOutputStream(basePath + sep + "chatLog.txt", true)){
                        file1.write(mes.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(sender + " SENT A VOICE");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
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
                        try(FileOutputStream file1 = new FileOutputStream(basePath + sep + "chatLog.txt", true)){
                            file1.write(mes.getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(sender + " SENT A VIDEO");
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
