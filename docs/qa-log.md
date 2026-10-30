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
