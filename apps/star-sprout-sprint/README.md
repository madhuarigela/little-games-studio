# Star Sprout Sprint

An original, offline Android endless runner for the Little Games Studio collection. It includes three lanes, swipe movement, jump and slide controls, seed collection, hazards, three biomes, unlockable looks, saved progress, pause/results screens, and original synthesized music and effects.

## Build

Open this folder in Android Studio or run `./gradlew :app:assembleDebug :app:bundleRelease` with JDK 17 and Android SDK platform 37 installed. A GitHub Actions build runs on changes and saves both the installable debug APK and release AAB as workflow artifacts.

The debug APK is for device review. The release AAB produced by CI is unsigned; configure a privately stored Play upload key before submitting it to Play Console. Never commit signing keys.

## Privacy and release readiness

The current game has no network permission, advertising, analytics, account, or data collection. The in-app privacy screen describes that behavior. Before Play submission, publish a privacy-policy URL and confirm the policy matches the final build, complete Play Console's Data safety and target-audience declarations, create store listing graphics/screenshots, and sign the release bundle with the developer's upload key.
