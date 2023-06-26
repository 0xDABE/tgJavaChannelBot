# Telegram Channel Bot Logger and Syncronizer

#### Requerements
### For build (Maven)
- Python 3 (must be in PATH, check it in terminal by "python")
- JDK 1.8
  
### For launching from jar
- [Java Runtime 8](https://www.java.com/en/download/manual.jsp) (for Jar in release, JDK no needed for jar)


#### Instructions
### Build
- Clone repository or download .zip
- Open folder with Java IDE (Intellij IDEA for example)
- Maven will automatically downaload neccesary files
- Set config.txt (you can rename it in Main.java)
- run Main.java
### Launching from Jar
- Download release build
- Unpack files from archieve to new folder
- Set config
- Run terminal and set working directory to folder with unpacked files
  It may be like this:
  ## Linux
  ```shell
  cd /home/Jeff/bot
  ```
  ## Windows
  ```shell
  cd C:\Users\Jeff\Documents\bot
  ```
- Run
  ```shell
  java -jar tgJavaBotTestMain.jar
  ```


- You can use Java terminal to sent text messages as Bot if you set ChatID in config file.
- All user's sent data will be downloaded in your BaseFolder/TgUserName.
- ChatLog is in BaseFolder.

