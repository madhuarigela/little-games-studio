# Architecture — Star Sprout Sprint

## Current implementation
- Single Android app module, Java 17, custom `GameView` Canvas loop, `MainActivity`, `GameData` SharedPreferences, `SoundEngine` AudioTrack.
- Package `com.madhuarigela.starsproutsprint`; minSdk 24 (Google Mobile Ads requirement), compileSdk 37, targetSdk 36.
- Current implementation remains Java 17 and Canvas, with consent-gated Google Mobile Ads/UMP, Google Play Billing, local purchase signature verification, and no app backend/analytics.

## Target separation
```
app/src/main/java/.../
  ui/ (Activity, Canvas/screens, input)
  game/ (RunnerEngine, entities, collision, difficulty, deterministic rules)
  domain/ (RunResult, Currency, Cosmetic, RewardPolicy)
  data/ (GameRepository, versioned local save, migrations)
  monetization/ads/ (Consent, AdRepository, frequency policy, test/prod IDs)
  monetization/billing/ (BillingRepository, entitlement, purchase verification)
  security/ (purchase verifier contract; no secrets)
app/src/test/       pure rules/economy/frequency unit tests
app/src/androidTest/ launch, lifecycle, persistence instrumentation tests
```

## Data model / save
- `RunResult(distanceMeters, seedsEarned, runId, endedAt)`.
- `PlayerSave(schemaVersion, bestDistance, softCurrency, selectedWorld, selectedCosmetic, ownedCosmetics, audioEnabled, tutorialSeen)`.
- Local preferences are convenience state only. Never store secrets or treat local paid currency as authoritative.
- Add explicit bounds and migration tests before changing save format. Paid entitlements are re-queried from Play; consumable grants need server verification/idempotency.

## Monetization implementation
- `AdsManager` requests UMP consent on launch, initializes Mobile Ads only when `canRequestAds()` permits it, exposes privacy options, and loads rewarded/interstitial inventory. Debug uses Google's official test IDs; release values come from ignored `local.properties`. Rewarded video is opt-in for three diamonds. Interstitials occur only after run completion with a 180-second cooldown.
- `BillingManager` connects to Play Billing 9.1, queries localized one-time product prices, launches purchase flows, restores owned purchases, acknowledges remove-ads, and consumes the diamond pack. It grants only `PURCHASED` items with matching app ID and a valid Play licensing signature. A SHA-256 digest of each consumable token is recorded atomically with the diamond grant to avoid duplicate rewards.
- `PurchaseSignatureVerifier` checks signed purchase JSON using the Play licensing public key. This is client-side protection; refunds/revocations/rooted-device tampering are not authoritatively handled. Server-side Play Developer API validation remains recommended before selling valuable consumables.
- `apps/star-sprout-sprint/local.properties.example` documents release IDs, placeholder product IDs, and the public licensing key. The real `local.properties` is ignored by Git.

## Security and release
- No committed signing keys, service-account JSON, passwords, or real ad IDs.
- R8 minification for release, least permissions, HTTPS only if networking is introduced.
- CI signing is conditional on secrets; unsigned artifacts remain available for CI only. AdMob and Play account configuration and physical store-flow checks are NOT VERIFIED.

## Migration note
The current green prototype is Java. User asked for Kotlin or Flutter. Migrate the Java classes incrementally to Kotlin before major feature growth or explicitly approve Java retention; build and instrumented coverage must stay green during migration. No migration is claimed complete.

