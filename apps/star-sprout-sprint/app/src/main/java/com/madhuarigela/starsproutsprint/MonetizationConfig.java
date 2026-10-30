package com.madhuarigela.starsproutsprint;

/** Validates AdMob configuration before any SDK or consent flow is started. */
final class MonetizationConfig {
    private MonetizationConfig() { }

    static boolean isReady(String appId, String rewardedUnitId, String interstitialUnitId) {
        return isAppId(appId)
                && isAdUnitId(rewardedUnitId)
                && isAdUnitId(interstitialUnitId);
    }

    static boolean isAppId(String value) {
        if (value == null || !value.matches("ca-app-pub-[0-9]{16}~[0-9]{10}")) {
            return false;
        }
        String[] parts = value.substring("ca-app-pub-".length()).split("~");
        return !isAllZeros(parts[0]) && !isAllZeros(parts[1]);
    }

    static boolean isAdUnitId(String value) {
        if (value == null || !value.matches("ca-app-pub-[0-9]{16}/[0-9]{10}")) {
            return false;
        }
        String[] parts = value.substring("ca-app-pub-".length()).split("/");
        return !isAllZeros(parts[0]) && !isAllZeros(parts[1]);
    }

    private static boolean isAllZeros(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }
}
