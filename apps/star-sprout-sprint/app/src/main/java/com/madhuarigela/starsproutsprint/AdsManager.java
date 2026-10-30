package com.madhuarigela.starsproutsprint;

import android.app.Activity;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/** Consent-gated AdMob wrapper. Rewards are always optional and user-initiated. */
final class AdsManager {
    interface RewardListener { void onRewardEarned(); }

    private final Activity activity;
    private final GameData data;
    private ConsentInformation consentInformation;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private boolean initialized;

    AdsManager(Activity activity, GameData data) {
        this.activity = activity;
        this.data = data;
    }

    void requestConsentAndLoad() {
        if (!isConfigured()) return;
        consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        consentInformation.requestConsentInfoUpdate(activity, params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,
                        error -> initializeIfAllowed()),
                error -> initializeIfAllowed());
    }

    private boolean isConfigured() {
        return MonetizationConfig.isReady(BuildConfig.ADMOB_APP_ID,
                BuildConfig.ADMOB_REWARDED_UNIT_ID, BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID);
    }

    private void initializeIfAllowed() {
        if (initialized || consentInformation == null || !consentInformation.canRequestAds()) return;
        initialized = true;
        MobileAds.initialize(activity, status -> {
            loadRewarded();
            loadInterstitial();
        });
    }

    void showPrivacyOptions() {
        if (consentInformation == null) return;
        if (consentInformation.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED) {
            UserMessagingPlatform.showPrivacyOptionsForm(activity, error -> initializeIfAllowed());
        }
    }

    void showRewarded(RewardListener listener) {
        if (!initialized || data.adsRemoved()) {
            Toast.makeText(activity, "Ad rewards are unavailable right now.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rewardedAd == null) {
            Toast.makeText(activity, "The optional reward video is not ready yet. Try again later.", Toast.LENGTH_SHORT).show();
            loadRewarded();
            return;
        }
        RewardedAd ready = rewardedAd;
        rewardedAd = null;
        ready.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() { loadRewarded(); }
            @Override public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError error) {
                loadRewarded();
            }
        });
        ready.show(activity, reward -> listener.onRewardEarned());
    }

    void showInterstitialAfterRun() {
        if (!initialized || data.adsRemoved()) return;
        long now = System.currentTimeMillis();
        long last = data.lastInterstitialAt();
        // Start the cooldown on the first completed run; never interrupt a user's first run.
        if (last == 0L) {
            data.setLastInterstitialAt(now);
            return;
        }
        if (!GameRules.isInterstitialEligible(last, now) || interstitialAd == null) return;
        InterstitialAd ready = interstitialAd;
        interstitialAd = null;
        ready.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdShowedFullScreenContent() { data.setLastInterstitialAt(System.currentTimeMillis()); }
            @Override public void onAdDismissedFullScreenContent() { loadInterstitial(); }
            @Override public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError error) {
                loadInterstitial();
            }
        });
        ready.show(activity);
    }

    private void loadRewarded() {
        if (!initialized || data.adsRemoved()) return;
        RewardedAd.load(activity, BuildConfig.ADMOB_REWARDED_UNIT_ID, new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override public void onAdLoaded(RewardedAd ad) { rewardedAd = ad; }
                    @Override public void onAdFailedToLoad(LoadAdError error) { rewardedAd = null; }
                });
    }

    private void loadInterstitial() {
        if (!initialized || data.adsRemoved()) return;
        InterstitialAd.load(activity, BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID,
                new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                    @Override public void onAdLoaded(InterstitialAd ad) { interstitialAd = ad; }
                    @Override public void onAdFailedToLoad(LoadAdError error) { interstitialAd = null; }
                });
    }
}

