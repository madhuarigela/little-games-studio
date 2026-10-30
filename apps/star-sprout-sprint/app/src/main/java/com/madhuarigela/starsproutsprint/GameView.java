Warning: truncated output (original token count: 8984)
Total output lines: 619

package com.madhuarigela.starsproutsprint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.View;
import android.view.HapticFeedbackConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

final class GameView extends View {
    private static final int HOME = 0, RUN = 1, PAUSE = 2, RESULT = 3,
            WORLDS = 4, WARDROBE = 5, SETTINGS = 6, PRIVACY = 7, SHOP = 8;
    // Keep custom action ids below the framework-reserved action id range.
    private static final int A_PLAY = 0x00002101, A_WORLDS = 0x00002102,
            A_WARDROBE = 0x00002103, A_SETTINGS = 0x00002104,
            A_PAUSE = 0x00002105, A_RESUME = 0x00002106,
            A_HOME = 0x00002107, A_LEFT = 0x00002108,
            A_RIGHT = 0x00002109, A_JUMP = 0x0000210a,
            A_SLIDE = 0x0000210b, A_WORLD0 = 0x0000210c,
            A_WORLD1 = 0x0000210d, A_WORLD2 = 0x0000210e,
            A_OUTFIT0 = 0x0000210f, A_OUTFIT1 = 0x00002110,
            A_OUTFIT2 = 0x00002111, A_OUTFIT3 = 0x00002112,
            A_SOUND = 0x00002113, A_TUTORIAL = 0x00002114,
            A_PRIVACY = 0x00002115, A_HAPTICS = 0x00002116, A_SHOP = 0x00002117,
            A_SHOP_HOME = 0x00002118, A_PRIVACY_OPTIONS = 0x00002119;
    private int lastA11yScreen = -1;
    private static final String[] WORLD_NAMES = {"Cloud Meadow", "Sunset Orchard", "Crystal Grove"};
    private static final String[] OUTFIT_NAMES = {"Sprout", "Sunbeam", "Berry", "Starlight"};
    private static final int[] OUTFIT_COLORS = {0xffffd36c, 0xffffa64d, 0xffe887ae, 0xff9ba7f5};
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final GameData data;
    private final SoundEngine sounds;
    private final Random random = new Random();
    private final List<Entity> entities = new ArrayList<>();
    private int screen = HOME, lane = 1, runSeeds, meters, selectedWorld;
    private int jumpFrames, slideFrames, outfitToastFrames, tutorialFrames;
    private float scale = 1f, virtualHeight = 844f, distance, spawnAt = 12f, anim;
    private long lastFrame = SystemClock.uptimeMillis();
    private float downX, downY;
    private boolean pointerHandled, appPaused;
    private String toast = "";
    private String removeAdsPrice = "Play setup required";
    private String diamondPackPrice = "Play setup required";

    GameView(Context context, GameData data, SoundEngine sounds) {
        super(context);
        this.data = data;
        this.sounds = sounds;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setFocusable(true);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName("android.view.View");
                addAccessibleActions(info);
            }
            @Override public boolean performAccessibilityAction(View host, int action, android.os.Bundle args) {
                if (performAccessibleAction(action)) return true;
                return super.performAccessibilityAction(host, action, args);
            }
        });
        refreshAccessibilityDescription();
        selectedWorld = data.world();
    }

    void onHostPause() {
        appPaused = true;
        if (screen == RUN) screen = PAUSE;
        invalidate();
    }

    void onHostResume() {
        appPaused = false;
        lastFrame = SystemClock.uptimeMillis();
        postInvalidateOnAnimation();
    }

    boolean handleBack() {
        if (screen == HOME) return false;
        if (screen == RUN) screen = PAUSE;
        else if (screen == PAUSE) screen = RUN;
        else screen = HOME;
        refreshAccessibilityDescription();
        invalidate();
        sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        return true;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        scale = getWidth() / 390f;
        virtualHeight = getHeight() / scale;
        canvas.save();
        canvas.scale(scale, scale);
        float h = virtualHeight;
        float dt = Math.min(.045f, Math.max(0f, (SystemClock.uptimeMillis() - lastFrame) / 1000f));
        lastFrame = SystemClock.uptimeMillis();
        anim += dt;
        update(dt);
        drawWorld(canvas, h, screen == RUN || screen == PAUSE);
        switch (screen) {
            case HOME: drawHome(canvas, h); break;
            case RUN: drawRun(canvas, h); break;
            case PAUSE: drawRun(canvas, h); drawOverlay(canvas, h, "PAUSED", "Take a little breather", "RESUME", "HOME"); break;
            case RESULT: drawResult(canvas, h); break;
            case WORLDS: drawWorldPicker(canvas, h); break;
            case WARDROBE: drawWardrobe(canvas, h); break;
            case SETTINGS: drawSettings(canvas, h); break;
            case PRIVACY: drawPrivacy(canvas, h); break;
            case SHOP: drawShop(canvas, h); break;
            default: drawHome(canvas, h);
        }
        if (outfitToastFrames > 0) { outfitToastFrames--; drawToast(canvas, h, toast); }
        canvas.restore();
        refreshAccessibilityDescription();
        // Animate only during an active run. Static menus should go idle to save battery and let UI automation settle.
        if (!appPaused && screen == RUN) postInvalidateOnAnimation();
    }

    private void refreshAccessibilityDescription() {
        if (lastA11yScreen == screen) return;
        lastA11yScreen = screen;
        String description;
        switch (screen) {
            case HOME: description = "Star Sprout Sprint home. Best " + data.bestMeters() + " metres. " + data.seeds() + " seeds. Swipe sideways to change lanes, swipe up to jump, swipe down to slide. Available actions: Play, Worlds, Wardrobe, Settings, Garden shop."; break;
            case RUN: description = "Run in progress. " + meters + " metres, " + runSeeds + " seeds collected. Swipe sideways to change lanes, up to jump, down to slide. Available actions: Move left, Move right, Jump, Slide, Pause."; break;
            case PAUSE: description = "Run paused at " + meters + " metres. Available actions: Resume, Home."; break;
            case RESULT: description = "Run complete. " + meters + " metres and " + runSeeds + " seeds collected. Available actions: Run again, Home."; break;
            case WORLDS: description = "Choose a garden world. Available actions: Cloud Meadow, Sunset Orchard, Crystal Grove, Home."; break;
            case WARDROBE: description = "Choose a sprout outfit. Available actions: Sprout, Sunbeam, Berry, Starlight, Home."; break;
            case SETTINGS: description = "Settings. Sound is " + (data.soundOn() ? "on" : "off") + ", haptics are " + (data.hapticsOn() ? "on" : "off") + ". Available actions: Toggle sound, Toggle haptics, Replay controls, Privacy, Home."; break;
            case SHOP: description = "Garden shop. " + data.diamonds() + " diamonds. Available actions: Watch for free diamonds, Remove ads, Buy a diamond pack, Restore purchases, Home."; break;
            case PRIVACY: description = "Privacy details. Available actions: Ad privacy options, Settings."; break;
            default: description = "Star Sprout Sprint.";
        }
        setContentDescription(description);
    }

    private static void addAction(AccessibilityNodeInfo info, int id, String label) {
        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(id, label));
    }

    private void addAccessibleActions(AccessibilityNodeInfo info) {
        switch (screen) {
            case HOME: addAction(info, A_PLAY, "Play"); addAction(info, A_WORLDS, "Worlds"); addAction(info, A_WARDROBE, "Wardrobe"); addAction(info, A_SETTINGS, "Settings"); addAction(info, A_SHOP_HOME, "Garden shop"); break;
            case RUN: addAction(info, A_LEFT, "Move left"); addAction(info, A_RIGHT, "Move right"); addAction(info, A_JUMP, "Jump"); addAction(info, A_SLIDE, "Slide"); addAction(info, A_PAUSE, "Pause"); break;
            case PAUSE: addAction(info, A_RESUME, "Resume"); addAction(info, A_HOME, "Home"); break;
            case RESULT: addAction(info, A_PLAY, "Run again"); addAction(info, A_HOME, "Home"); break;
            case WORLDS: addAction(info, A_WORLD0, WORLD_NAMES[0]); addAction(info, A_WORLD1, WORLD_NAMES[1]); addAction(info, A_WORLD2, WORLD_NAMES[2]); addAction(info, A_HOME, "Home"); break;
            case WARDROBE: addAction(info, A_OUTFIT0, OUTFIT_NAMES[0]); addAction(info, A_OUTFIT1, OUTFIT_NAMES[1]); addAction(info, A_OUTFIT2, OUTFIT_NAMES[2]); addAction(info, A_OUTFIT3, OUTFIT_NAMES[3]); addAction(info, A_HOME, "Home"); break;
            case SETTINGS: addAction(info, A_SOUND, "Toggle sound"); addAction(info, A_HAPTICS, "Toggle haptics"); addAction(info, A_TUTORIAL, "Replay controls"); addAction(info, A_PRIVACY, "Privacy"); addAction(info, A_HOME, "Home"); break;
            case PRIVACY: addAction(info, A_PRIVACY_OPTIONS, "Ad privacy options"); addAction(info, A_SETTINGS, "Settings"); break;
            case SHOP: addAction(info, A_SHOP, "Watch for free diamonds"); addAction(info, A_SHOP + 1, "Remove ads"); addAction(info, A_SHOP + 2, "Buy a diamond pack"); addAction(info, A_SHOP + 3, "Restore purchases"); addAction(info, A_HOME, "Home"); break;
        }
    }

    private boolean performAccessibleAction(int action) {
        sounds.button();
        haptic();
        switch (action) {
            case A_PLAY: if (screen == HOME || screen == RESULT) beginRun(); else return false; break;
            case A_WORLDS: if (screen != HOME) return false; screen = WORLDS; break;
            case A_WARDROBE: if (screen != HOME) return false; screen = WARDROBE; break;
            case A_SETTINGS: if (screen == HOME || screen == PRIVACY) screen = SETTINGS; else return false; break;
            case A_PAUSE: if (screen != RUN) return false; screen = PAUSE; break;
            case A_RESUME: if (screen != PAUSE) return false; screen = RUN; break;
            case A_HOME: if (screen == HOME) return false; screen = HOME; break;
            case A_LEFT: if (screen != RUN) return false; moveLane(-1); break;
            case A_RIGHT: if (screen != RUN) return false; moveLane(1); break;
            case A_JUMP: if (screen != RUN) return false; jump(); break;
            case A_SLIDE: if (screen != RUN) return false; slide(); break;
            case A_WORLD0: if (screen != WORLDS) return false; selectWorld(0); break;
            case A_WORLD1: if (screen != WORLDS) return false; selectWorld(1); break;
            case A_WORLD2: if (screen != WORLDS) return false; selectWorld(2); break;
            case A_OUTFIT0: if (screen != WARDROBE) return false; chooseOutfit(0); break;
            case A_OUTFIT1: if (screen != WARDROBE) return false; chooseOutfit(1); break;
            case A_OUTFIT2: if (screen != WARDROBE) return false; chooseOutfit(2); break;
            case A_OUTFIT3: if (screen != WARDROBE) return false; chooseOutfit(3); break;
            case A_SOUND: if (screen != SETTINGS) return false; data.setSoundOn(!data.soundOn()); sounds.setEnabled(data.soundOn()); break;
            case A_HAPTICS: if (screen != SETTINGS) return false; data.setHapticsOn(!data.hapticsOn()); break;
            case A_TUTORIAL: if (screen != SETTINGS) return false; screen = HOME; toast = "Swipe left/right, up to hop, down to slide"; outfitToastFrames = 160; break;
            case A_PRIVACY: if (screen != SE…2984 tokens truncated…54, "BACK", 0xfffbf3d7, 0xff326e64);
    }

    private void drawShop(Canvas c, float h) {
        header(c, "GARDEN SHOP", h);
        centered(c, "DIAMONDS  " + data.diamonds(), 18, 0xff7359ad, 180, true);
        button(c, 35, 215, 320, 68, "WATCH AD  ·  +3 DIAMONDS", 0xffdcefcf, 0xff326e64);
        button(c, 35, 305, 320, 68, data.adsRemoved() ? "ADS REMOVED" : "REMOVE ADS  ·  " + removeAdsPrice, 0xfffbf3d7, 0xff326e64);
        button(c, 35, 395, 320, 68, "100 DIAMONDS  ·  " + diamondPackPrice, 0xffeee4ff, 0xff5b4d91);
        button(c, 35, 485, 320, 58, "RESTORE PURCHASES", 0xfffbf3d7, 0xff326e64);
        centered(c, "Store items activate after your Play setup", 11, 0xff52766b, 574, false);
        button(c, 85, h - 95, 220, 54, "BACK", 0xfffbf3d7, 0xff326e64);
    }

    private void drawPrivacy(Canvas c, float h) {
        header(c, "PRIVACY NOTE", h);
        rounded(c, 32, 198, 326, 370, 24, 0xfffff6df);
        centered(c, "Your garden stays on this device", 17, 0xff245b5d, 243, true);
        centered(c, "Google may process ad requests after", 13, 0xff426e66, 290, false);
        centered(c, "you review its consent choices.", 13, 0xff426e66, 313, false);
        centered(c, "Play handles optional purchases;", 13, 0xff426e66, 352, false);
        centered(c, "there is no game account.", 13, 0xff426e66, 375, false);
        button(c, 53, 445, 284, 58, "AD PRIVACY OPTIONS", 0xffdcefcf, 0xff326e64);
        centered(c, "Scores, seeds and settings are stored locally.", 11, 0xff52766b, 532, false);
        button(c, 85, h - 95, 220, 54, "BACK", 0xfffbf3d7, 0xff326e64);
    }

    private void header(Canvas c, String title, float h) {
        centered(c, title, 25, 0xff205a5e, 112, true);
        centered(c, "A GARDEN FULL OF LITTLE SURPRISES", 10, 0xff548174, 140, true);
    }

    private void drawBadge(Canvas c, float x, float y, float s, int outfit) {
        c.save(); c.translate(x, y); c.scale(s, s);
        p.setColor(0x33295145); c.drawOval(-42, 52, 42, 66, p);
        p.setColor(0xfff6a57c); c.drawRoundRect(-42, 27, -21, 57, 12, 12, p); c.drawRoundRect(21, 27, 42, 57, 12, 12, p);
        p.setColor(0xffe9b755); c.drawRoundRect(-35, -36, 35, 43, 29, 29, p);
        p.setColor(OUTFIT_COLORS[Math.max(0, Math.min(outfit, OUTFIT_COLORS.length - 1))]); c.drawRoundRect(-35, 14, 35, 43, 17, 17, p);
        p.setColor(0xff5da86c);
        Path leaf1 = new Path(); leaf1.moveTo(-1, -32); leaf1.cubicTo(-28, -68, -48, -61, -42, -42); leaf1.cubicTo(-31, -26, -11, -27, -1, -32); leaf1.close(); c.drawPath(leaf1, p);
        Path leaf2 = new Path(); leaf2.moveTo(1, -32); leaf2.cubicTo(28, -68, 48, -61, 42, -42); leaf2.cubicTo(31, -26, 11, -27, 1, -32); leaf2.close(); c.drawPath(leaf2, p);
        p.setColor(0xfffff3ca); c.drawOval(-22, -6, -8, 12, p); c.drawOval(8, -6, 22, 12, p);
        p.setColor(0xff244b4b); c.drawCircle(-15, 3, 4.5f, p); c.drawCircle(15, 3, 4.5f, p);
        p.setColor(0xffdc806d); c.drawOval(-5, 16, 5, 23, p);
        p.setColor(0xfff19977); c.drawCircle(-26, 16, 5, p); c.drawCircle(26, 16, 5, p);
        c.restore();
    }

    private void drawCloud(Canvas c, float x, float y, float s) {
        p.setColor(0xcfffffff); c.drawCircle(x, y, 19 * s, p); c.drawCircle(x + 22 * s, y - 8 * s, 25 * s, p); c.drawCircle(x + 49 * s, y + 1 * s, 18 * s, p); c.drawRect(x - 1 * s, y, x + 47 * s, y + 14 * s, p);
    }

    private void drawHill(Canvas c, float x, float y, float w, float h, int color) {
        p.setColor(color); Path path = new Path(); path.moveTo(x, y); path.quadTo(x + w * .45f, y - h, x + w, y); path.lineTo(x + w, 650); path.lineTo(x, 650); path.close(); c.drawPath(path, p);
    }

    private void flower(Canvas c, float x, float y, float s) {
        p.setColor(0xffffefc5); c.drawCircle(x - 4 * s, y, 4 * s, p); c.drawCircle(x + 4 * s, y, 4 * s, p); c.drawCircle(x, y - 4 * s, 4 * s, p); c.drawCircle(x, y + 4 * s, 4 * s, p); p.setColor(0xffefaa58); c.drawCircle(x, y, 2.5f * s, p);
    }

    private void control(Canvas c, float x, float y, String label) {
        p.setColor(0xbffff8df); c.drawCircle(x, y, 29, p); p.setColor(0xff27645f); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(32); p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); c.drawText(label, x, y + 11, p);
    }

    private void button(Canvas c, float x, float y, float w, float h, String label, int fill, int ink) {
        p.setColor(0x3324534d); c.drawRoundRect(x, y + 4, x + w, y + h + 4, h / 2, h / 2, p);
        p.setColor(fill); c.drawRoundRect(x, y, x + w, y + h, h / 2, h / 2, p);
        p.setColor(ink); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(Math.min(20, h * .34f)); p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); c.drawText(label, x + w / 2, y + h / 2 + p.getTextSize() * .34f, p);
    }

    private void rounded(Canvas c, float x, float y, float w, float h, float r, int color) {
        p.setColor(color); c.drawRoundRect(x, y, x + w, y + h, r, r, p);
    }

    private void centered(Canvas c, String text, float size, int color, float baseline, boolean bold) {
        p.setColor(color); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(size); p.setTypeface(android.graphics.Typeface.create(bold ? "sans-serif-rounded" : "sans-serif", bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL)); c.drawText(text, 195, baseline, p);
    }

    private void drawToast(Canvas c, float h, String text) {
        rounded(c, 48, h - 150, 294, 46, 23, 0xeefff7dc);
        centered(c, text, 13, 0xff245b5d, h - 121, true);
    }

    private float laneX(int lane, float depth) {
        float[] near = {70, 195, 320}, far = {175, 195, 215};
        return far[lane] + (near[lane] - far[lane]) * depth;
    }

    private void beginRun() {
        screen = RUN; distance = 0; meters = 0; runSeeds = 0; lane = 1; jumpFrames = slideFrames = 0;
        entities.clear(); spawnAt = 10; random.setSeed(SystemClock.uptimeMillis()); lastFrame = SystemClock.uptimeMillis();
    }

    private void finishRun() {
        data.finishRun(meters, runSeeds);
        screen = RESULT;
        ((MainActivity) getContext()).onRunFinished();
    }

    private void jump() { if (jumpFrames == 0 && slideFrames == 0) { jumpFrames = 46; sounds.jump(); } }
    private void slide() { if (slideFrames == 0 && jumpFrames == 0) slideFrames = 38; }

    private void moveLane(int direction) { lane = Math.max(0, Math.min(2, lane + direction)); }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX() / scale; downY = event.getY() / scale; pointerHandled = false; return true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE && screen == RUN && !pointerHandled) {
            float dx = event.getX() / scale - downX, dy = event.getY() / scale - downY;
            if (Math.abs(dx) > 44 || Math.abs(dy) > 50) {
                if (Math.abs(dx) > Math.abs(dy)) moveLane(dx > 0 ? 1 : -1); else if (dy < 0) jump(); else slide();
                haptic();
                pointerHandled = true; data.setTutorialSeen(); return true;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX() / scale, y = event.getY() / scale;
            if (!pointerHandled) handleTap(x, y);
            performClick(); return true;
        }
        return true;
    }

    @Override public boolean performClick() { super.performClick(); return true; }

    private void handleTap(float x, float y) {
        float h = virtualHeight;
        sounds.button();
        haptic();
        if (screen == HOME) {
            if (in(x,y,55,h-300,280,64)) beginRun();
            else if (in(x,y,43,h-218,145,54)) screen = WORLDS;
            else if (in(x,y,202,h-218,145,54)) screen = WARDROBE;
            else if (in(x,y,43,h-147,145,48)) screen = SETTINGS;
            else if (in(x,y,202,h-147,145,48)) screen = SHOP;
        } else if (screen == RUN) {
            if (in(x,y,319,24,54,48)) screen = PAUSE;
            else if (y > h - 125) {
                if (x < 95) moveLane(-1); else if (x < 190) jump(); else if (x < 285) slide(); else moveLane(1);
                data.setTutorialSeen();
            } else if (y < 110 && x > 285) screen = PAUSE;
        } else if (screen == PAUSE) {
            if (y > h/2 - 15 && y < h/2 + 54) screen = RUN;
            else if (y > h/2 + 55) screen = HOME;
        } else if (screen == RESULT) {
            if (y > h - 230 && y < h - 166) beginRun();
            else if (y > h - 162 && y < h - 114) screen = SHOP;
            else if (y > h - 106) screen = HOME;
        } else if (screen == WORLDS) {
            if (y >= 210 && y < 322) { selectWorld(0); }
            else if (y >= 352 && y < 464) { selectWorld(1); }
            else if (y >= 494 && y < 606) { selectWorld(2); }
            else if (y > h - 120) screen = HOME;
        } else if (screen == WARDROBE) {
            if (y >= 365 && y < 448) chooseOutfit(x < 195 ? 0 : 1);
            else if (y >= 471 && y < 554) chooseOutfit(x < 195 ? 2 : 3);
            else if (y > h - 120) screen = HOME;
        } else if (screen == SETTINGS) {
            if (y >= 180 && y < 260) { data.setSoundOn(!data.soundOn()); sounds.setEnabled(data.soundOn()); }
            else if (y >= 260 && y < 340) data.setHapticsOn(!data.hapticsOn());
            else if (y >= 340 && y < 420) { screen = HOME; toast = "Swipe left/right, up to hop, down to slide"; outfitToastFrames = 160; }
            else if (y >= 420 && y < 500) screen = PRIVACY;
            else if (y > h - 120) screen = HOME;
        } else if (screen == PRIVACY) {
            if (y >= 445 && y < 503) ((MainActivity) getContext()).showPrivacyOptions();
            else if (y > h - 120) screen = SETTINGS;
        } else if (screen == SHOP) {
            MainActivity host = (MainActivity) getContext();
            if (y >= 215 && y < 283) host.showRewardedAd();
            else if (y >= 305 && y < 373) host.purchaseRemoveAds();
            else if (y >= 395 && y < 463) host.purchaseDiamondPack();
            else if (y >= 485 && y < 543) host.restorePurchases();
            else if (y > h - 120) screen = HOME;
        }
        refreshAccessibilityDescription();
        invalidate();
        sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    private void haptic() {
        if (data.hapticsOn()) performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    void onStoreChanged(String message) {
        toast = message;
        outfitToastFrames = 100;
        refreshAccessibilityDescription();
        invalidate();
    }

    void onStorePrices(String removePrice, String diamondPrice) {
        removeAdsPrice = removePrice;
        diamondPackPrice = diamondPrice;
        invalidate();
    }

    private void selectWorld(int index) {
        boolean unlocked = index == 0 || (index == 1 && data.bestMeters() >= 350) || (index == 2 && data.bestMeters() >= 1100);
        if (unlocked) { selectedWorld = index; data.selectWorld(index); }
        else { toast = "Reach " + (index == 1 ? "350" : "1,100") + " m to unlock"; outfitToastFrames = 120; }
    }

    private void chooseOutfit(int index) {
        if (index >= OUTFIT_NAMES.length) return;
        int price = index * 35;
        if (data.buyOutfit(index, price)) toast = OUTFIT_NAMES[index] + " is ready!";
        else toast = "Collect a few more seeds first";
        outfitToastFrames = 120;
    }

    private boolean in(float x, float y, float left, float top, float width, float height) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }

    private static final class Entity {
        final int lane, type;
        float depth;
        boolean hitChecked, collected;
        Entity(int lane, int type, float depth) { this.lane = lane; this.type = type; this.depth = depth; }
    }
}

