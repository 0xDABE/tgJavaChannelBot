public class langDetect {
    public static boolean containsCyrillic(String text) {
        return text.matches(".*[а-яА-Я].*");
    }
    public static boolean containsJapanese(String text){
        return text.matches(".*[\\p{IsHiragana}\\p{IsKatakana}].*");
    }
}
