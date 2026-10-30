# Architecture — Star Sprout Sprint

## Current implementation
- Single Android app module, Java 17, custom `GameView` Canvas loop, `MainActivity`, `GameData` SharedPreferences, `SoundEngine` AudioTrack.
- Package `com.madhuarigela.starsproutsprint`; minSdk 23, compileSdk 37, targetSdk 36.
- No backend, ads, billing, analytics, or network permission in the green baseline.

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

## Monetization interfaces (target)
- `AdsRepository.initializeAfterConsent`, `showRewarded`, `showInterstitialIfEligible`; test IDs in debug; production IDs in owner-provided config; no ad during active game.
- `BillingRepository.connect`, `queryProducts`, `purchase`, `queryOwned`, `acknowledge`; placeholder product IDs until Play Console setup.
- `PurchaseVerifier` is a server-side contract. Without a backend credential and Play Developer API setup, paid consumable packs must remain disabled.

## Security and release
- No committed signing keys, service-account JSON, passwords, or real ad IDs.
- R8 minification for release, least permissions, HTTPS only if networking is introduced.
- CI signing is conditional on secrets; unsigned artifacts remain available for CI only.

## Migration note
The current green prototype is Java. User asked for Kotlin or Flutter. Migrate the Java classes incrementally to Kotlin before major feature growth or explicitly approve Java retention; build and instrumented coverage must stay green during migration. No migration is claimed complete.
