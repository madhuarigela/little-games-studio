# Manual device test checklist — Star Sprout Sprint

Record device model, Android version, app version, date, tester, and pass/fail for every check. These checks are NOT VERIFIED until a human runs them on physical devices.

## Install / launch / lifecycle
- [ ] Install debug APK from the CI artifact; cold launch; verify no crash or clipped UI.
- [ ] Background/foreground during menu and during run; run pauses safely and audio stops/resumes correctly.
- [ ] Receive a call/notification or open quick settings while running; verify pause and resume.
- [ ] Rotate device (app is portrait locked); verify stable layout and no state loss.
- [ ] Force-stop/relaunch; best distance, seed count, settings, world, and outfit persist.
- [ ] Clear storage/uninstall; app starts cleanly.

## Gameplay / controls
- [ ] First-run instructions explain swipe left/right, up to jump, down to slide.
- [ ] Each swipe works repeatedly; rapid gestures don't trigger multiple accidental actions.
- [ ] All three lanes, jumping, sliding, obstacle hit, seed collection, pause/resume, game-over/retry work.
- [ ] World and outfit selection; purchase with earned in-game seeds; insufficient funds; progress persists.
- [ ] Back button behavior is consistent on every screen.
- [ ] Sound toggle works; audio does not continue after leaving app; no clipping at quiet/full device volume.

## Device / accessibility / performance
- [ ] Small 5-inch screen, large phone, and tablet layout; text does not clip.
- [ ] Android font/display scaling at 1.0, 1.3, and 1.5; interactive targets remain usable.
- [ ] TalkBack labels and focus order; contrast in sunlight; color is not sole state indicator.
- [ ] Low-end device sustained play: frame pacing, heat, memory, battery, and audio assessed with profiler.
- [ ] Low-memory process recreation, storage pressure, and slow startup.
- [ ] Airplane mode/no network: core game remains playable.
- [ ] Ad failure/no fill and billing unavailable never block gameplay (after integration).
- [ ] Test purchase/cancel/restore/refund and purchase acknowledgment (after billing integration).
