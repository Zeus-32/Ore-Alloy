package eu.zunix.ore_and_alloy.worldgen.vein;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OAVeinText {
    private OAVeinText() {}

    public static String toDisplayName(String token) {
        String normalized = token == null ? "" : token.toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) return normalized;
        String[] words = normalized.replace('-', '_').split("_");
        List<String> out = new ArrayList<>(words.length);
        for (String word : words) {
            if (word.isBlank()) continue;
            out.add(word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1));
        }
        return String.join(" ", out);
    }
}

