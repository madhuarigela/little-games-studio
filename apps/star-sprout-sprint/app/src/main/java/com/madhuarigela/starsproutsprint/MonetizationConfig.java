package com.madhuarigela.starsproutsprint;

/** Validates AdMob configuration before any SDK or consent flow is started. */
final class MonetizationConfig {
    private MonetizationConfig() { }
    static boolean isReady(String appId, String rewardedUnitId, String interstitialUnitId) {
        return isAppId(appId) && isAdUnitId(rewardedUnitId) && isAdUnitId(interstitialUnitId);
    }
    static boolean isAppId(String value) {
        return value != null && value.matches("ca-app-pub-[0-9]{16}~[0-9]{10}");
    }
    static boolean isAdUnitId(String value) {
        return value != null && value.matches("ca-app-pub-[0-9]{16}/[0-9]{10}");
    }
}
