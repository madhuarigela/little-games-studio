# Product specification — Star Sprout Sprint

## Product
A bright, original portrait endless runner for general audiences age 13+. Players guide a tiny seedling courier across a floating garden path, dodge nature-themed hazards, collect seed tokens, and unlock original palettes/outfits and biomes. Offline core game; no account required.

## Audience and boundaries
- General audience 13+, not designed or marketed as a child-directed app.
- No copied characters, named worlds, music, level layout, UI, or store assets. Use broad runner genre conventions only.
- Accessibility: readable type, high contrast, touch inputs, sound toggle, avoid color-only feedback; add reduced motion if feasible.

## Core feature set
- Three-lane endless run; swipe lane movement, jump, slide; obstacles, seed pickups, pause, results, restart.
- Three original biome palettes; earned-seed cosmetic unlocks; locally saved best score and options.
- First-run control tutorial, synthesized original loop and effects, settings/privacy access.
- Offline play. Monetization is planned but must not block core play.

## Monetization plan (proposed, not active)
1. Optional rewarded ad: explicit opt-in to double earned seeds after a completed run; grant only from the reward callback and only once per run.
2. Interstitial: only at a natural run boundary and at least 180 seconds since the last interstitial; never during play, onboarding, or immediately on launch. Skip/close remains system-controlled.
3. One-time remove-ads product; optional diamond packs only after server-side purchase verification and clear virtual-item terms. No pay-to-win pressure.
4. Debug uses Google's official test ad unit IDs. Production IDs remain in a single local/CI config input and are never enabled until the owner configures AdMob, consent, declarations, and release review.
5. Purchases use Play Billing; acknowledge promptly, restore/query owned purchases, verify on server before granting consumables. Client signature checks alone are not adequate protection for consumable currency.

## Milestones
- M0 playable baseline: DONE (prototype and green APK/AAB build).
- M1 product/UX/architecture and CI tests: IN PROGRESS.
- M2 gameplay polish/accessibility/performance: NOT VERIFIED.
- M3 consent-safe monetization and purchase backend: NOT STARTED.
- M4 privacy/security/release workflow and store assets: DRAFT.
- M5 device QA, closed testing, Play release: BLOCKED on owner accounts, signing key, device QA, and console declarations.

## Risks
- The game is a prototype; device play and sustained performance are unverified.
- Ads and billing introduce data handling and policy disclosures; SDK versions and consent behavior require review before release.
- Local-only seed progress is resettable and unsuitable as authority for paid currency.
- Child users may still play a general-audience game; store audience and content rating declarations must be truthful, and ads must follow applicable policy.
- Store/Play review outcomes cannot be guaranteed by code changes.
