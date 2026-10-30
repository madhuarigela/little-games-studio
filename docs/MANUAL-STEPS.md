# Manual steps for the owner — Star Sprout Sprint

1. Create/verify your Google Play Console developer account and complete identity, payments, tax, and contact setup.
2. Create/verify your AdMob account only when ready to monetize; create app/ad units. Use Google demo IDs in debug. Keep production IDs in local `local.properties` or GitHub Secrets and never commit them.
3. Generate a dedicated upload keystore on your own machine, keep a secure offline backup, and never send it in chat or commit it. Example (replace values locally): `keytool -genkeypair -v -keystore starsprout-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload`.
4. Add GitHub Actions secrets for `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEY_ALIAS`, `ANDROID_KEYSTORE_PASSWORD`, and `ANDROID_KEY_PASSWORD`; base64 encode the keystore locally. Configure production AdMob IDs and any billing backend credentials only after the code and privacy flow are ready. Never put secrets in repository files.
5. Configure Google Play App Signing in Play Console and upload the upload certificate. Keep your upload key safe; Play App Signing does not make a lost upload key harmless.
6. Create the app record and package `com.madhuarigela.starsproutsprint`; package name cannot be casually changed after publishing.
7. Host `docs/play-store/privacy-policy.md` on GitHub Pages at a stable public URL, add real contact details, and update the policy to match every SDK in the final bundle. Add that exact URL in Play Console and in-app.
8. Complete Data Safety, Ads, target audience (general 13+), content rating, app access, and other Play Console declarations based on the exact release binary. Draft answers in `docs/play-store/declarations.md` are not a substitute for your review.
9. Create original 512×512 icon, 1024×500 feature graphic, and real screenshots per `docs/play-store/listing.md`.
10. Configure Billing product IDs and prices in Play Console; add the exact IDs to the single app config after backend verification is available. Use license testers/test tracks to validate purchase, cancel, restore, refund, and acknowledgment.
11. Test on physical Android devices using `docs/test-checklist-device.md`; record device/version/results and fix all P0/P1 issues.
12. Upload signed AAB from the green CI artifact to internal testing first. Verify on test accounts; then run the required closed test. For accounts subject to the current personal-account gate, recruit at least 12 testers opted in continuously for 14 days; confirm exact eligibility in Console.
13. Review pre-launch report, crash/ANR, policy warnings, store listing, privacy URL, and declarations. Promote to production only after all checks pass.
14. Download APK for device review: GitHub repo → Actions → successful Android build → Artifacts → `star-sprout-sprint-build` → download/extract ZIP → install APK. The release AAB is only signed if signing secrets are configured in the workflow.

NOT VERIFIED: account setup, key generation, GitHub secret configuration, privacy hosting, production IDs, Play declarations, real-device testing, tester recruitment, and Play review.
