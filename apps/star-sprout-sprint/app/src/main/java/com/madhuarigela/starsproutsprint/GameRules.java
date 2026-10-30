package com.madhuarigela.starsproutsprint;

/** Pure game policies kept independent of Android for deterministic tests. */
final class GameRules {
    static final long INTERSTITIAL_COOLDOWN_MS = 180_000L;

    private GameRules() { }

    static boolean canAfford(int balance, int price) {
        return balance >= 0 && price >= 0 && balance >= price;
    }

    static boolean isInterstitialEligible(long lastShownAtMs, long nowMs) {
        if (lastShownAtMs <= 0L) return true;
        if (nowMs < lastShownAtMs) return false;
        return nowMs - lastShownAtMs >= INTERSTITIAL_COOLDOWN_MS;
    }

    static int safeCurrencyAdd(int balance, int earned) {
        if (balance < 0 || earned < 0) return Math.max(0, balance);
        long total = (long) balance + earned;
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    static int safeDistance(int meters) {
        return Math.max(0, meters);
    }
}
