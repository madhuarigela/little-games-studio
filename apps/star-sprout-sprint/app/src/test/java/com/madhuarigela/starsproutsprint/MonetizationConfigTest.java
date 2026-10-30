package com.madhuarigela.starsproutsprint;

import org.junit.Test;
import static org.junit.Assert.*;

public class MonetizationConfigTest {
    private static final String APP = "ca-app-pub-3940256099942544~3347511713";
    private static final String REWARDED = "ca-app-pub-3940256099942544/5224354917";
    private static final String INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712";
    @Test public void acceptsOfficialTestAdMobConfiguration() {
        assertTrue(MonetizationConfig.isReady(APP, REWARDED, INTERSTITIAL));
    }
    @Test public void rejectsPlaceholderAppId() {
        assertFalse(MonetizationConfig.isReady("ca-app-pub-0000000000000000~0000000000", REWARDED, INTERSTITIAL));
    }
    @Test public void rejectsPlaceholderOrMalformedAdUnitIds() {
        assertFalse(MonetizationConfig.isReady(APP, "ca-app-pub-0000000000000000/0000000000", INTERSTITIAL));
        assertFalse(MonetizationConfig.isReady(APP, REWARDED, "not-an-ad-unit"));
    }
    @Test public void rejectsNullValues() {
        assertFalse(MonetizationConfig.isReady(null, REWARDED, INTERSTITIAL));
        assertFalse(MonetizationConfig.isReady(APP, null, INTERSTITIAL));
    }
}
