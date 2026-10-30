package com.madhuarigela.starsproutsprint;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

final class SoundEngine {
    private static final int RATE = 22050;
    private volatile boolean enabled = true;
    private volatile boolean paused;
    private volatile boolean released;
    private volatile float effectFrequency;
    private volatile int effectFrames;
    private Thread worker;
    private AudioTrack track;

    SoundEngine() { start(); }

    void setEnabled(boolean value) {
        enabled = value;
        if (track != null) track.setVolume(value && !paused ? .48f : 0f);
    }

    void pause() {
        paused = true;
        if (track != null) { track.setVolume(0f); track.pause(); }
    }

    void resume() {
        paused = false;
        if (track != null && !released) { track.setVolume(enabled ? .48f : 0f); track.play(); }
    }

    void collect() { effect(880f); }
    void jump() { effect(590f); }
    void hit() { effect(180f); }
    void button() { effect(490f); }

    private void effect(float frequency) {
        if (!enabled || paused || released) return;
        effectFrequency = frequency;
        effectFrames = RATE / 6;
    }

    private void start() {
        int minimum = AudioTrack.getMinBufferSize(RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(Math.max(minimum, 4096))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
        track.setVolume(enabled ? .48f : 0f);
        track.play();
        worker = new Thread(this::renderLoop, "sprout-music");
        worker.setDaemon(true);
        worker.start();
    }

    private void renderLoop() {
        short[] samples = new short[1024];
        double samplePosition = 0, effectPhase = 0;
        int[] melody = {587, 440, 494, 659, 587, 392, 440, 523, 659, 587, 494, 440, 392, 440, 523, 587};
        int[] bass = {147, 147, 165, 165, 196, 196, 165, 165};
        int beatFrames = RATE / 3;
        while (!released) {
            for (int i = 0; i < samples.length; i++) {
                int beat = (int)(samplePosition / beatFrames);
                int within = (int)(samplePosition % beatFrames);
                double t = samplePosition / RATE;
                double noteT = (double)within / beatFrames;
                double envelope = Math.max(0, 1.0 - noteT * 1.6);
                double lead = Math.sin(2 * Math.PI * melody[beat % melody.length] * t) * envelope * .18;
                double pad = Math.sin(2 * Math.PI * bass[(beat / 2) % bass.length] * t) * .065;
                double chime = 0;
                if (effectFrames > 0 && enabled && !paused) {
                    double e = (double)effectFrames / (RATE / 6);
                    chime = Math.sin(effectPhase) * e * .24;
                    effectPhase += 2 * Math.PI * effectFrequency / RATE;
                    effectFrames--;
                } else if (effectFrames <= 0) effectPhase = 0;
                double value = Math.max(-.8, Math.min(.8, lead + pad + chime));
                samples[i] = (short)(value * Short.MAX_VALUE);
                samplePosition++;
            }
            if (released) break;
            int written = track.write(samples, 0, samples.length, AudioTrack.WRITE_BLOCKING);
            if (written < 0) break;
        }
    }

    void release() {
        released = true;
        if (track != null) {
            try { track.pause(); track.flush(); track.stop(); } catch (IllegalStateException ignored) { }
            track.release();
            track = null;
        }
        if (worker != null) worker.interrupt();
    }
}
