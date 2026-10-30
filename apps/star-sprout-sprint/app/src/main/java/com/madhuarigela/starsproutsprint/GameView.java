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
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

final class GameView extends View {
    private static final int HOME = 0, RUN = 1, PAUSE = 2, RESULT = 3,
            WORLDS = 4, WARDROBE = 5, SETTINGS = 6, PRIVACY = 7;
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

    GameView(Context context, GameData data, SoundEngine sounds) {
        super(context);
        this.data = data;
        this.sounds = sounds;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setFocusable(true);
        setContentDescription("Star Sprout Sprint. Swipe sideways to change lanes, swipe up to jump, and swipe down to slide. Collect seeds and avoid obstacles.");
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
        if (screen == RUN) { screen = PAUSE; return true; }
        if (screen == PAUSE) { screen = RUN; return true; }
        if (screen != HOME) { screen = HOME; return true; }
        return false;
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
            default: drawHome(canvas, h);
        }
        if (outfitToastFrames > 0) { outfitToastFrames--; drawToast(canvas, h, toast); }
        canvas.restore();
        if (!appPaused) postInvalidateOnAnimation();
    }

    private void update(float dt) {
        if (screen != RUN) return;
        distance += dt * (13f + Math.min(11f, distance / 130f));
        meters = (int) distance;
        if (jumpFrames > 0) jumpFrames--;
        if (slideFrames > 0) slideFrames--;
        if (distance >= spawnAt) {
            int type = random.nextInt(4);
            int entityLane = random.nextInt(3);
            entities.add(new Entity(entityLane, type, -.02f));
            if (random.nextFloat() < .46f) {
                int seedLane = (entityLane + 1 + random.nextInt(2)) % 3;
                entities.add(new Entity(seedLane, 3, -.14f));
            }
            spawnAt = distance + 14f + random.nextInt(11);
        }
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            e.depth += dt * (.25f + Math.min(.16f, distance / 1800f));
            if (!e.hitChecked && e.depth >= .86f) {
                e.hitChecked = true;
                if (e.lane == lane && e.type == 3) {
                    runSeeds++;
                    sounds.collect();
                    e.collected = true;
                } else if (e.lane == lane && e.type != 3) {
                    boolean safe = (e.type == 0 && jumpFrames > 0) || (e.type == 1 && slideFrames > 0);
                    if (!safe) { sounds.hit(); finishRun(); return; }
                }
            }
            if (e.depth > 1.18f || e.collected) it.remove();
        }
    }

    private void drawWorld(Canvas c, float h, boolean road) {
        int world = selectedWorld;
        int skyTop = world == 1 ? 0xffffad89 : world == 2 ? 0xff91a0dc : 0xff71cfe0;
        int skyBottom = world == 1 ? 0xffffe1a5 : world == 2 ? 0xffd5c8ef : 0xffd5f4e5;
        p.setShader(new LinearGradient(0, 0, 0, h, skyTop, skyBottom, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, 390, h, p); p.setShader(null);
        p.setColor(world == 2 ? 0xffeee4ff : 0xfffff1b1);
        c.drawCircle(315, 115 + (float)Math.sin(anim * .3) * 5, 34, p);
        drawCloud(c, 60 + (float)Math.sin(anim * .18) * 8, 150, 1.0f);
        drawCloud(c, 280 + (float)Math.cos(anim * .16) * 10, 215, .72f);
        drawHill(c, -25, 430, 250, 135, world == 1 ? 0xffd98572 : world == 2 ? 0xff7f8ec2 : 0xff74c99d);
        drawHill(c, 170, 405, 300, 170, world == 1 ? 0xffc97973 : world == 2 ? 0xff7486ba : 0xff58b989);
        drawHill(c, -65, 475, 300, 115, world == 1 ? 0xff91c17e : world == 2 ? 0xffa5a2cf : 0xff43a972);
        if (road) drawTrack(c, h);
        else {
            p.setColor(0xff5db878); c.drawRect(0, 568, 390, h, p);
            for (int i = 0; i < 12; i++) {
                float x = (i * 47 + (anim * 9) % 47);
                flower(c, x, 588 + (i % 3) * 15, .65f);
            }
        }
    }

    private void drawTrack(Canvas c, float h) {
        float horizon = 390f;
        Path path = new Path();
        path.moveTo(160, horizon); path.lineTo(230, horizon); path.lineTo(390, h); path.lineTo(0, h); path.close();
        p.setColor(0xffd9bd86); c.drawPath(path, p);
        Path center = new Path(); center.moveTo(184, horizon); center.lineTo(206, horizon); center.lineTo(245, h); center.lineTo(145, h); center.close();
        p.setColor(0xffefd9a8); c.drawPath(center, p);
        for (int laneLine = 0; laneLine < 2; laneLine++) {
            float topX = laneLine == 0 ? 178 : 202, bottomX = laneLine == 0 ? 130 : 260;
            p.setColor(0xffc7a978); p.setStrokeWidth(2); c.drawLine(topX, horizon, bottomX, h, p);
        }
        p.setColor(0xff51ae76); c.drawRect(0, h - 45, 390, h, p);
        for (int i = 0; i < 10; i++) flower(c, (i * 53 + (float)(distance * 4 % 53)), h - 38 - (i % 2) * 13, .5f);
    }

    private void drawHome(Canvas c, float h) {
        centered(c, "STAR SPROUT", 32, 0xff1f5b5e, 102, true);
        centered(c, "SPRINT", 46, 0xfff4a545, 151, true);
        drawBadge(c, 195, 320 + (float)Math.sin(anim * 2) * 5, 1.25f, data.outfit());
        centered(c, "Run, hop and collect little seeds!", 16, 0xff316d68, 414, false);
        centered(c, "BEST  " + data.bestMeters() + " m       SEEDS  " + data.seeds(), 15, 0xff23595c, 451, true);
        button(c, 55, h - 300, 280, 64, "PLAY", 0xfff39849, 0xffffffff);
        button(c, 43, h - 218, 145, 54, "WORLDS", 0xfffbf3d7, 0xff326e64);
        button(c, 202, h - 218, 145, 54, "WARDROBE", 0xfffbf3d7, 0xff326e64);
        button(c, 116, h - 147, 158, 48, "SETTINGS", 0xfffbf3d7, 0xff326e64);
        centered(c, "A cozy little run through the garden", 12, 0xff386d62, h - 58, false);
    }

    private void drawRun(Canvas c, float h) {
        centered(c, meters + " m", 25, 0xff21585d, 51, true);
        centered(c, "✦ " + runSeeds, 18, 0xff23595c, 82, true);
        button(c, 319, 24, 54, 48, "Ⅱ", 0xfffbf3d7, 0xff326e64);
        for (Entity e : entities) drawEntity(c, e, h);
        float groundY = h - 155;
        float jump = jumpFrames > 0 ? (float)Math.sin((1f - jumpFrames / 46f) * Math.PI) * 95f : 0;
        if (slideFrames > 0) drawBadge(c, laneX(lane, .96f), groundY - 9, .82f, data.outfit());
        else drawBadge(c, laneX(lane, .96f), groundY - jump, 1f, data.outfit());
        controls(c, h);
        if (!data.tutorialSeen() && distance < 12) {
            rounded(c, 38, 125, 314, 74, 20, 0xeefeffe7);
            centered(c, "Swipe to move  •  ↑ jump  •  ↓ slide", 14, 0xff245c5d, 154, true);
            centered(c, "Collect seeds and dodge the garden bits", 12, 0xff477a6f, 177, false);
        }
    }

    private void controls(Canvas c, float h) {
        float y = h - 82;
        control(c, 48, y, "‹"); control(c, 142, y, "↑"); control(c, 236, y, "↓"); control(c, 330, y, "›");
    }

    private void drawEntity(Canvas c, Entity e, float h) {
        float d = Math.max(0f, Math.min(1f, e.depth));
        float x = laneX(e.lane, d);
        float y = 393 + d * (h - 550);
        float size = 12 + d * 36;
        p.setColor(0x330d4c42); c.drawOval(x - size * .75f, y + size * .55f, x + size * .75f, y + size * .87f, p);
        if (e.type == 3) {
            p.setColor(0xffffd65f); c.drawCircle(x, y, size * .55f, p);
            p.setColor(0xfffff4b7); c.drawCircle(x - size * .16f, y - size * .16f, size * .15f, p);
            p.setColor(0xff6cae60); c.drawOval(x - size * .65f, y - size * .92f, x - size * .05f, y - size * .38f, p);
        } else if (e.type == 0) {
            p.setColor(0xffae7569); c.drawRoundRect(x - size, y - size * .55f, x + size, y + size * .65f, size * .3f, size * .3f, p);
            p.setColor(0xffd59c80); c.drawCircle(x - size * .35f, y - size * .1f, size * .18f, p);
        } else if (e.type == 1) {
            p.setColor(0xff497c56); c.drawRoundRect(x - size * 1.2f, y - size * .8f, x + size * 1.2f, y - size * .25f, size * .3f, size * .3f, p);
            p.setColor(0xff67a85d); c.drawOval(x - size * .95f, y - size * 1.2f, x - size * .15f, y - size * .4f, p);
        } else {
            p.setColor(0xff67a95b); c.drawRoundRect(x - size * 1.2f, y - size * .7f, x + size * 1.2f, y + size * .6f, size * .25f, size * .25f, p);
            p.setColor(0xffffd66c); c.drawCircle(x - size * .35f, y - size * .14f, size * .12f, p); c.drawCircle(x + size * .35f, y - size * .14f, size * .12f, p);
        }
    }

    private void drawResult(Canvas c, float h) {
        drawBadge(c, 195, 220, 1.2f, data.outfit());
        centered(c, "RUN COMPLETE", 30, 0xff1f5b5e, 334, true);
        centered(c, meters + " metres", 22, 0xffed9441, 381, true);
        centered(c, "+ " + runSeeds + " seeds collected", 17, 0xff316d68, 420, false);
        centered(c, "BEST  " + data.bestMeters() + " m     TOTAL SEEDS  " + data.seeds(), 14, 0xff23595c, 453, true);
        button(c, 55, h - 230, 280, 60, "RUN AGAIN", 0xfff39849, 0xffffffff);
        button(c, 90, h - 151, 210, 52, "GARDEN HOME", 0xfffbf3d7, 0xff326e64);
    }

    private void drawOverlay(Canvas c, float h, String title, String subtitle, String primary, String secondary) {
        p.setColor(0x770f3e42); c.drawRect(0, 0, 390, h, p);
        rounded(c, 35, h / 2 - 145, 320, 290, 28, 0xfffff6df);
        centered(c, title, 29, 0xff205a5e, h / 2 - 84, true);
        centered(c, subtitle, 15, 0xff52766b, h / 2 - 51, false);
        button(c, 63, h / 2 - 10, 264, 56, primary, 0xfff39849, 0xffffffff);
        button(c, 91, h / 2 + 62, 208, 48, secondary, 0xffe6efd8, 0xff326e64);
    }

    private void drawWorldPicker(Canvas c, float h) {
        header(c, "CHOOSE A WORLD", h);
        for (int i = 0; i < WORLD_NAMES.length; i++) {
            float y = 210 + i * 142;
            boolean unlocked = i == 0 || (i == 1 && data.bestMeters() >= 350) || (i == 2 && data.bestMeters() >= 1100);
            int color = i == 0 ? 0xffb8ebdf : i == 1 ? 0xffffd2a7 : 0xffd5cff4;
            rounded(c, 35, y, 320, 112, 23, color);
            centered(c, WORLD_NAMES[i], 20, 0xff245b5d, y + 40, true);
            centered(c, unlocked ? (selectedWorld == i ? "SELECTED  •  TAP TO PLAY" : "UNLOCKED  •  TAP TO SELECT") : "LOCKED  •  REACH " + (i == 1 ? "350" : "1,100") + " m", 12, 0xff4d746c, y + 72, false);
        }
        button(c, 85, h - 95, 220, 54, "BACK", 0xfffbf3d7, 0xff326e64);
    }

    private void drawWardrobe(Canvas c, float h) {
        header(c, "LITTLE WARDROBE", h);
        centered(c, "Seeds collected   ✦ " + data.seeds(), 15, 0xff316d68, 170, true);
        drawBadge(c, 195, 263, 1.12f, data.outfit());
        for (int i = 0; i < OUTFIT_NAMES.length; i++) {
            int col = i % 2, row = i / 2;
            float x = 35 + col * 164, y = 365 + row * 106;
            rounded(c, x, y, 156, 83, 19, 0xfffff5dc);
            p.setColor(OUTFIT_COLORS[i]); c.drawCircle(x + 30, y + 30, 15, p);
            centered(c, OUTFIT_NAMES[i], 14, 0xff245b5d, y + 34, true);
            String action = data.outfit() == i ? "EQUIPPED" : data.ownsOutfit(i) ? "WEAR" : (i * 35) + " SEEDS";
            centered(c, action, 10, 0xff52766b, y + 61, false);
        }
        button(c, 85, h - 95, 220, 54, "BACK", 0xfffbf3d7, 0xff326e64);
    }

    private void drawSettings(Canvas c, float h) {
        header(c, "SETTINGS", h);
        button(c, 42, 240, 306, 62, "SOUND  " + (data.soundOn() ? "ON" : "OFF"), 0xfffbf3d7, 0xff326e64);
        button(c, 42, 325, 306, 62, "HOW TO PLAY", 0xfffbf3d7, 0xff326e64);
        button(c, 42, 410, 306, 62, "PRIVACY", 0xfffbf3d7, 0xff326e64);
        centered(c, "No account needed. Your garden stays on this device.", 12, 0xff52766b, 518, false);
        button(c, 85, h - 95, 220, 54, "BACK", 0xfffbf3d7, 0xff326e64);
    }

    private void drawPrivacy(Canvas c, float h) {
        header(c, "PRIVACY NOTE", h);
        rounded(c, 32, 198, 326, 370, 24, 0xfffff6df);
        centered(c, "A quiet little game", 20, 0xff245b5d, 243, true);
        centered(c, "Star Sprout Sprint works offline.", 15, 0xff426e66, 290, false);
        centered(c, "It has no account, ads, analytics,", 14, 0xff426e66, 329, false);
        centered(c, "or network connection.", 14, 0xff426e66, 352, false);
        centered(c, "Scores and seeds are saved locally", 14, 0xff426e66, 401, false);
        centered(c, "on this device. Uninstalling the game", 14, 0xff426e66, 424, false);
        centered(c, "may remove that saved progress.", 14, 0xff426e66, 447, false);
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
        if (screen == HOME) {
            if (in(x,y,55,h-300,280,64)) beginRun();
            else if (in(x,y,43,h-218,145,54)) screen = WORLDS;
            else if (in(x,y,202,h-218,145,54)) screen = WARDROBE;
            else if (in(x,y,116,h-147,158,48)) screen = SETTINGS;
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
            if (y > h - 245 && y < h - 158) beginRun(); else if (y > h - 158) screen = HOME;
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
            if (y >= 230 && y < 310) { data.setSoundOn(!data.soundOn()); sounds.setEnabled(data.soundOn()); }
            else if (y >= 315 && y < 400) { screen = HOME; toast = "Swipe left/right, up to hop, down to slide"; outfitToastFrames = 160; }
            else if (y >= 400 && y < 485) screen = PRIVACY;
            else if (y > h - 120) screen = HOME;
        } else if (screen == PRIVACY && y > h - 120) screen = SETTINGS;
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
