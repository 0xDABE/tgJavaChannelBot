#     Telegram Channel Bot Logger and Syncronizer

# Requerements
### For build (Maven)
- [Python 3](https://www.python.org/downloads/) (must be in PATH, check it in terminal by "python")
- [JDK 1.8](https://www.oracle.com/uk/java/technologies/downloads/) or higher
  
### For launching from jar
- [Java Runtime 8](https://www.java.com/en/download/manual.jsp) (for Jar in release, JDK no needed for jar)


# Instructions
### Build
- Clone repository
  ```shell
  git clone https://github.com/0xDABE/tgJavaChannelBot
  ```
  or download .zip archieve
- Open folder with Java IDE ([Intellij IDEA](https://www.jetbrains.com/idea/) for example)
- Maven will automatically downaload neccesary files
- Set config.txt (you can rename it in Main.java)
- run Main.java
  
### Launching from Jar
- Download release build
- Unpack files from archieve to new folder
- Set config
- Run terminal and set working directory to folder with unpacked files
  It may be like this (for example):
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
  java -jar tgJavaBotTestMaven.jar
  ```
  On windows, better experience with [Windows Terminal](https://apps.microsoft.com/store/detail/windows-terminal/9N0DX20HK701)

# Commands
## help
  ```shell
  /help
  ```
 ### shows help menu. List of available commands. Different in direct messages and in channels.

 
## calc
```Python
/calc 12**2+6
```
### Calculates python regex (spaces doesn't matter)
#### Usage: /calc \<Python regex>

## shell
```Python
/shell neofetch
```
### shell prompt. Intercepts shell's output stream to direct message. Each shell prompt runs in different sessions.
### You can run shell commands in one session by | symbol, for example:
```bash
/shell cat .config/mpv/mpv.conf | grep gpu
```
#### Usage: /shell \<prompt>

## tr
```shell
/tr en|fr hello
```
### Translates text in a given languages
#### Usage: /tr \<srcLanguage>|\<destLang> \<srcText>
### Use
```shell
/tr
```
### to send text from LanguageFile.txt to show language codes list 
### Using /tr without language set, bot automatically translates text from ru to en, or from en to ru
```shell
/tr desk
```


# Extra
- You can use Java terminal to sent text messages as Bot if you set ChatID in config file.
- All user's sent data will be downloaded in your BaseFolder/TgUserName.
- ChatLog is in BaseFolder.

