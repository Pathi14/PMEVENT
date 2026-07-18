package fr.pmevent.common.utils;

public class StringUtils {
    private StringUtils() {
    }

    public static String capitalizeWords(String str) {
        if (str == null || str.isBlank()) return "";
        String[] words = str.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
