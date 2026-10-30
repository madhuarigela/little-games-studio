package com.madhuarigela.starsproutsprint;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public final class MainActivity extends Activity {
    private GameView gameView;
    private SoundEngine soundEngine;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(0xff67c9dc);
        window.setNavigationBarColor(0xff173d46);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        GameData data = new GameData(this);
        soundEngine = new SoundEngine();
        soundEngine.setEnabled(data.soundOn());
        gameView = new GameView(this, data, soundEngine);
        setContentView(gameView);
        hideSystemBars();
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
        if (soundEngine != null) soundEngine.release();
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (gameView != null && gameView.handleBack()) return;
        super.onBackPressed();
    }
}
