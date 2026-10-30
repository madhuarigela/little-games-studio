package com.madhuarigela.starsproutsprint;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

final class GameData {
    private static final int WORLD_COUNT = 3;
    private static final int OUTFIT_COUNT = 4;
    private final SharedPreferences prefs;

    GameData(Context context) {
        prefs = context.getSharedPreferences("star_sprout_save", Context.MODE_PRIVATE);
    }

    int bestMeters() { return GameRules.safeDistance(prefs.getInt("best_meters", 0)); }
    int seeds() { return Math.max(0, prefs.getInt("seeds", 0)); }
    int diamonds() { return Math.max(0, prefs.getInt("diamonds", 0)); }
    boolean adsRemoved() { return prefs.getBoolean("ads_removed", false); }
    long lastInterstitialAt() { return Math.max(0L, prefs.getLong("last_interstitial_at", 0L)); }
    int world() { return clamp(prefs.getInt("world", 0), 0, WORLD_COUNT - 1); }
    int outfit() { return clamp(prefs.getInt("outfit", 0), 0, OUTFIT_COUNT - 1); }
    boolean soundOn() { return prefs.getBoolean("sound_on", true); }
    boolean hapticsOn() { return prefs.getBoolean("haptics_on", true); }
    boolean tutorialSeen() { return prefs.getBoolean("tutorial_seen", false); }

    void setSoundOn(boolean value) { prefs.edit().putBoolean("sound_on", value).apply(); }
    void setHapticsOn(boolean value) { prefs.edit().putBoolean("haptics_on", value).apply(); }
    void addDiamonds(int amount) {
        if (amount <= 0) return;
        prefs.edit().putInt("diamonds", GameRules.safeCurrencyAdd(diamonds(), amount)).apply();
    }
    void setAdsRemoved() { prefs.edit().putBoolean("ads_removed", true).apply(); }
    void setLastInterstitialAt(long timeMs) {
        if (timeMs > 0) prefs.edit().putLong("last_interstitial_at", timeMs).apply();
    }
    boolean hasProcessedPurchase(String token) {
        return token != null && !token.isEmpty() && prefs.getBoolean("purchase_" + tokenDigest(token), false);
    }

    boolean grantDiamondsForPurchase(String token, int amount) {
        if (token == null || token.isEmpty() || amount <= 0) return false;
        String key = "purchase_" + tokenDigest(token);
        if (prefs.getBoolean(key, false)) return false;
        return prefs.edit().putInt("diamonds", GameRules.safeCurrencyAdd(diamonds(), amount))
                .putBoolean(key, true).commit();
    }

    private static String tokenDigest(String token) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
            return result.toString();
        } catch (Exception impossible) {
            throw new IllegalStateException("SHA-256 is required by Android", impossible);
        }
    }
    void setTutorialSeen() { prefs.edit().putBoolean("tutorial_seen", true).apply(); }
    void selectWorld(int value) {
        if (value >= 0 && value < WORLD_COUNT) prefs.edit().putInt("world", value).apply();
    }
    void selectOutfit(int value) {
        if (value >= 0 && value < OUTFIT_COUNT && ownsOutfit(value)) {
            prefs.edit().putInt("outfit", value).apply();
        }
    }

    void finishRun(int meters, int foundSeeds) {
        prefs.edit()
                .putInt("best_meters", Math.max(bestMeters(), GameRules.safeDistance(meters)))
                .putInt("seeds", GameRules.safeCurrencyAdd(seeds(), Math.max(0, foundSeeds)))
                .apply();
    }

    boolean buyOutfit(int index, int price) {
        if (index < 0 || index >= OUTFIT_COUNT || price < 0) return false;
        if (ownsOutfit(index)) {
            selectOutfit(index);
            return true;
        }
        int balance = seeds();
        if (!GameRules.canAfford(balance, price)) return false;
        prefs.edit()
                .putInt("seeds", balance - price)
                .putBoolean("outfit_owned_" + index, true)
                .putInt("outfit", index)
                .apply();
        return true;
    }

    boolean ownsOutfit(int index) {
        return index == 0 || (index > 0 && index < OUTFIT_COUNT
                && prefs.getBoolean("outfit_owned_" + index, false));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

