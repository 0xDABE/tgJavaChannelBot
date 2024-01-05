import java.io.IOException;
import java.util.ArrayList;

public class ColoredMessage {
    public static boolean isWindows = System.getProperty("os.name").contains("Windows");

    public static void red(String message, boolean CompatibilityModeOff){
        if (!CompatibilityModeOff) System.out.print(message);
        else System.out.print("\u001B[31m" + message + "\u001B[0m");
    }
    public static void green(String message, boolean CompatibilityModeOff){
        if (!CompatibilityModeOff) System.out.print(message);
        else System.out.print("\u001B[32m" + message + "\u001B[0m");
    }
    public static void blue(String message, boolean CompatibilityModeOff){
        if (!CompatibilityModeOff) System.out.print(message);
        else System.out.print("\u001b[36m" + message + "\u001B[0m");
    }
    public static void yellow(String message, boolean CompatibilityModeOff){
        if (!CompatibilityModeOff) System.out.print(message);
        else System.out.print("\u001B[33m" + message + "\u001B[0m");
    }
    public static void clear(){
        ArrayList<String> command = new ArrayList<>();
        if (isWindows){
            command.add("cmd");
            command.add("/c");
            command.add("cls");
        }
        else{
            command.add("/bin/bash");
            command.add("-c");
            command.add("clear");
        }
        try{
            new ProcessBuilder(command).inheritIO().start().waitFor();
            command.clear();
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
}
