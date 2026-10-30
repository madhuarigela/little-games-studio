package com.madhuarigela.starsproutsprint;

import android.content.Context;
import android.content.SharedPreferences;

final class GameData {
    private final SharedPreferences prefs;

    GameData(Context context) {
        prefs = context.getSharedPreferences("star_sprout_save", Context.MODE_PRIVATE);
    }

    int bestMeters() { return prefs.getInt("best_meters", 0); }
    int seeds() { return prefs.getInt("seeds", 0); }
    int world() { return prefs.getInt("world", 0); }
    int outfit() { return prefs.getInt("outfit", 0); }
    boolean soundOn() { return prefs.getBoolean("sound_on", true); }
    boolean tutorialSeen() { return prefs.getBoolean("tutorial_seen", false); }

    void setSoundOn(boolean value) { prefs.edit().putBoolean("sound_on", value).apply(); }
    void setTutorialSeen() { prefs.edit().putBoolean("tutorial_seen", true).apply(); }
    void selectWorld(int value) { prefs.edit().putInt("world", value).apply(); }
    void selectOutfit(int value) { prefs.edit().putInt("outfit", value).apply(); }

    void finishRun(int meters, int foundSeeds) {
        prefs.edit()
                .putInt("best_meters", Math.max(bestMeters(), meters))
                .putInt("seeds", seeds() + foundSeeds)
                .apply();
    }

    boolean buyOutfit(int index, int price) {
        if (prefs.getBoolean("outfit_owned_" + index, false)) {
            selectOutfit(index);
            return true;
        }
        int balance = seeds();
        if (balance < price) return false;
        prefs.edit()
                .putInt("seeds", balance - price)
                .putBoolean("outfit_owned_" + index, true)
                .putInt("outfit", index)
                .apply();
        return true;
    }

    boolean ownsOutfit(int index) {
        return index == 0 || prefs.getBoolean("outfit_owned_" + index, false);
    }
}
