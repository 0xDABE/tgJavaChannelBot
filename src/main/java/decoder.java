import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class  decoder {
    public static String decodeUnicodeEscape(String input) {
        StringBuilder output = new StringBuilder();
        int length = input.length();
        int i = 0;

        while (i < length) {
            char currentChar = input.charAt(i);

            if (currentChar == '\\' && i + 1 < length && input.charAt(i + 1) == 'u') {
                if (i + 5 < length) {
                    String hexCode = input.substring(i + 2, i + 6);
                    try {
                        int unicodeValue = Integer.parseInt(hexCode, 16);
                        output.append((char) unicodeValue);
                        i += 6;
                        continue;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            output.append(currentChar);
            i++;
        }
        return output.toString();
    }

    public static String buildUrl(String baseUri, String queryParam1, String queryParam2) {
        String encodedParam1;
        String encodedParam2;

        try {
            encodedParam1 = URLEncoder.encode(queryParam1, StandardCharsets.UTF_8.toString());
            encodedParam2 = URLEncoder.encode(queryParam2, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode query parameters.", e);
        }

        return baseUri + "?q=" + encodedParam1 + "&langpair=" + encodedParam2;
    }
}