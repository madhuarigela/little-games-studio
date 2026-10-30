# QA log — Star Sprout Sprint

## 2026-10-30 — baseline audit (QA + security + compliance self-review)
- **Observed:** GitHub Actions run 41024997190 succeeded. `assembleDebug` and `bundleRelease` completed; artifact `star-sprout-sprint-build` was uploaded. Build config is compileSdk 37 / targetSdk 36 / minSdk 23.
- **Defect found and fixed before green:** Gradle wrapper script lacked executable permission and wrapper JAR transfer was incomplete. Corrected the executable mode and replaced the wrapper JAR; latest run is green.
- **Defect:** Workflow does not invoke unit or instrumented test tasks. **Status:** open; do not represent as tested.
- **Security:** No network permission, ads SDK, billing SDK, or secret is present in baseline. This reduces data collection but also means no monetization or store-ready purchase flow.
- **Privacy/compliance:** Local privacy draft describes current offline build only. It must be revised after adding any third-party SDK, purchase, analytics, or network behavior. Target audience is general 13+; actual content rating and declarations remain owner-confirmed in Play Console.
- **QA limits:** No test device, emulator UI test, 60 fps profile, battery test, interruption/low-memory test, purchase test, or ad-fill test was run. All are NOT VERIFIED.

## Defect tracking convention
Add each finding with date, severity (P0–P3), reproducible steps, expected/actual behavior, fix commit, and verification result. Never mark NOT VERIFIED as passed.

## 2026-10-30 — CI workflow validation failure
- **Severity:** P1 (release pipeline unable to start).
- **Reproduction:** Push workflow using `secrets.*` directly in step `if` expressions; Actions reported a failed run with no jobs.
- **Cause/fix:** GitHub does not allow secret context in `if`. Replaced with a dedicated step that emits a boolean output without printing the secret; conditional steps now use that output.
- **Verification:** Pending new CI run.

## 2026-10-30 — instrumentation runner stalled
- **Severity:** P2 (instrumented verification does not finish in expected CI window).
- **Observed:** Run 41029101220 passed JVM tests and artifact assembly but remained in the emulator-test step for over 12 minutes; the GitHub run-log endpoint did not expose live output while it was active. Root cause is NOT VERIFIED.
- **Mitigation:** Stop scheduling continuous Canvas redraws on static screens (active run remains frame-driven), which reduces idle battery use and lets UI automation settle. Set explicit emulator launch flags, disable emulator animations, and add a finite job timeout.
- **Verification:** Pending a subsequent CI run.

## 2026-10-30 — second emulator run stalled
- **Severity:** P2 (emulator instrumentation not completing).
- **Observed:** Run 41030690734 passed JVM tests and bundle assembly but remained at the emulator step. Root cause remains NOT VERIFIED.
- **Mitigation:** App has no Google Play Services dependency, so changed API 35 test image from `google_apis` to standard `default` image to reduce setup work. Prior static-redraw mitigation remains.
- **Verification:** Pending third CI run.

## 2026-10-30 — automated QA baseline green
- **Run:** 41030690734 (commit `d61bd821dbcdc0f123b595450353e1f347755d5f`).
- **Result:** PASS — four JVM unit tests, debug APK and release AAB assembly, emulator instrumentation launch/accessibility-action test, artifact upload.
- **Note:** Signing secrets were absent, so the release AAB is unsigned. No device QA was performed.

## 2026-10-30 — accessibility and haptics improvements
- **Changes:** Custom screen-reader actions for home, run, pause, results, world/outfit selection, and settings; dynamic screen descriptions; system haptics with an in-game toggle; Back now invalidates static screens correctly.
- **Self-review:** No new permission or network behavior. Haptics can be disabled. Canvas control hit targets still require physical-device measurements and TalkBack verification.
- **Verification:** Pending CI after commit.

## 2026-10-30 — accessibility instrumentation failed
- **Run:** 41048622797. JVM tests and APK/AAB assembly passed; emulator instrumentation failed.
- **Severity:** P2 (test harness defect; accessibility action presence itself was not disproved).
- **Reproduction:** Launch `MainActivityInstrumentedTest.launchesAndExposesGameInstructions`; iterate `AccessibilityNodeInfo.getActionList()` and compare every label with `"Play".contentEquals(label)`.
- **Cause:** Android includes standard accessibility actions whose labels are null; the test called `contentEquals` with a null argument, causing `NullPointerException` at line 26.
- **Fix:** Skip unlabeled actions before comparing the custom Play label. Added CI XML printing so future failures are visible in the job log; the captured XML confirmed the NPE.
- **Verification:** PASS in run 41050606144 (commit `78a5ba1f2d247bff9f55f5626e0892a3cb32ef51`): all four JVM unit tests, APK/AAB assembly, emulator instrumentation, and artifact upload completed successfully. Artifact `star-sprout-sprint-build` (ID `12975146658`) contains the debug APK, unsigned release AAB, and reports.

