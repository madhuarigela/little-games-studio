package com.madhuarigela.starsproutsprint;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class MainActivityInstrumentedTest {
    @Test public void launchesAndExposesGameInstructions() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View content = activity.getWindow().getDecorView().findViewById(android.R.id.content);
                assertNotNull(content);
                View game = ((android.view.ViewGroup) content).getChildAt(0);
                assertNotNull(game);
                assertTrue(game.getContentDescription().toString().contains("Swipe sideways"));
            });
        }
    }
}
