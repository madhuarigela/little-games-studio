package com.madhuarigela.starsproutsprint;

import org.junit.Test;
import static org.junit.Assert.*;

public final class GameRulesTest {
    @Test public void affordabilityRejectsNegativeValues() {
        assertTrue(GameRules.canAfford(5, 5));
        assertFalse(GameRules.canAfford(4, 5));
        assertFalse(GameRules.canAfford(-1, 0));
        assertFalse(GameRules.canAfford(5, -1));
    }

    @Test public void interstitialRequiresThreeMinuteCooldown() {
        assertTrue(GameRules.isInterstitialEligible(0L, 10L));
        assertFalse(GameRules.isInterstitialEligible(1_000L, 180_999L));
        assertTrue(GameRules.isInterstitialEligible(1_000L, 181_000L));
        assertFalse(GameRules.isInterstitialEligible(500L, 100L));
    }

    @Test public void currencyAdditionDoesNotOverflowOrGoNegative() {
        assertEquals(12, GameRules.safeCurrencyAdd(5, 7));
        assertEquals(Integer.MAX_VALUE, GameRules.safeCurrencyAdd(Integer.MAX_VALUE, 1));
        assertEquals(4, GameRules.safeCurrencyAdd(4, -1));
        assertEquals(0, GameRules.safeCurrencyAdd(-4, 1));
    }

    @Test public void distanceCannotBeNegative() {
        assertEquals(0, GameRules.safeDistance(-1));
        assertEquals(15, GameRules.safeDistance(15));
    }
}
