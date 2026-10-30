package com.madhuarigela.starsproutsprint;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public final class MainActivity extends Activity {
    private GameView gameView;
    private SoundEngine soundEngine;
    private GameData gameData;
    private AdsManager adsManager;
    private BillingManager billingManager;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(0xff67c9dc);
        window.setNavigationBarColor(0xff173d46);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        gameData = new GameData(this);
        soundEngine = new SoundEngine();
        soundEngine.setEnabled(gameData.soundOn());
        gameView = new GameView(this, gameData, soundEngine);
        setContentView(gameView);
        adsManager = new AdsManager(this, gameData);
        adsManager.requestConsentAndLoad();
        billingManager = new BillingManager(this, gameData);
        billingManager.connectAndRestore();
        hideSystemBars();
    }

    void onRunFinished() { if (adsManager != null) adsManager.showInterstitialAfterRun(); }
    void showRewardedAd() {
        if (adsManager != null) adsManager.showRewarded(() -> {
            gameData.addDiamonds(3);
            if (gameView != null) gameView.onStoreChanged("3 diamonds added");
        });
    }
    void purchaseRemoveAds() { if (billingManager != null) billingManager.purchaseRemoveAds(); }
    void purchaseDiamondPack() { if (billingManager != null) billingManager.purchaseDiamondPack(); }
    void restorePurchases() { if (billingManager != null) billingManager.restorePurchases(); }
    void showPrivacyOptions() { if (adsManager != null) adsManager.showPrivacyOptions(); }
    void onStorePrices(String removeAdsPrice, String diamondPackPrice) {
        if (gameView != null) gameView.onStorePrices(removeAdsPrice, diamondPackPrice);
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                5894 | 512 | 1024 | 4096);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    @Override protected void onPause() {
        if (gameView != null) gameView.onHostPause();
        if (soundEngine != null) soundEngine.pause();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        if (soundEngine != null) soundEngine.resume();
        if (gameView != null) gameView.onHostResume();
        hideSystemBars();
    }

    @Override protected void onDestroy() {
        if (billingManager != null) billingManager.close();
        if (soundEngine != null) soundEngine.release();
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (gameView != null && gameView.handleBack()) return;
        super.onBackPressed();
    }
}

