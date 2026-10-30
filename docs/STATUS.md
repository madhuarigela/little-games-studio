# Star Sprout Sprint — autonomous delivery status

Last updated: 2026-10-30

## Scope
- Active app: `apps/star-sprout-sprint` only. The other seven apps remain untouched placeholders until the owner authorizes a new app.
- Audience: general 13+ (owner selected).
- Originality: all characters, visuals, synthesized music, sound, world names, UI, and gameplay content are created for this project; genre conventions only.

## Stage tracker
| Stage | State | Notes |
|---|---|---|
| 0 Audit | DONE | GitHub Actions run 41024997190 green; debug APK and unsigned AAB artifact uploaded. |
| 1 Product spec | DONE | Spec and monetization/risk plan in `docs/play-store/product-spec.md`. |
| 2 UX | DONE | Flow, wireframes, and accessibility rules drafted in `docs/play-store/ux-spec.md`; UI changes continue in implementation. |
| 3 Architecture | DONE (design only) | Architecture and migration notes in `docs/play-store/architecture.md`; Java/Canvas migration mismatch remains open. |
| 4 Gameplay | IN PROGRESS | Playable offline prototype, bounds-safe save/economy helpers; active-run frame pacing only. 60 fps target NOT VERIFIED. |
| 5 Polish | IN PROGRESS | Original Canvas character, environments, generated audio; accessible screen actions and haptics implemented. Device review NOT VERIFIED. |
| 6 Monetization | IN PROGRESS | Google Mobile Ads + UMP, opt-in rewarded diamonds, 180-second interstitial cap, and Play Billing local signature verification implemented; account flows NOT VERIFIED. |
| 7 QA | IN PROGRESS | Latest run 41054618157: JVM tests PASS, APK/AAB assembly PASS, emulator instrumentation still running. Gameplay/device QA remains NOT VERIFIED. |
| 8 Security + compliance | IN PROGRESS | Privacy and Data Safety drafts now describe Google Ads/Billing; account-specific messages/forms, backend verification, and console declarations NOT VERIFIED. |
| 9 Release engineering | IN PROGRESS | CI emits debug APK and unsigned AAB. Conditional signing workflow and secrets instructions remain. |
| 10 Store pack | DRAFT | Listing, graphics specs, test plan are in `docs/play-store/`. |
| 11 Post-launch | DRAFT | Metrics plan is in `docs/play-store/post-launch.md`. |

## Green baseline
- Workflow: https://github.com/madhuarigela/little-games-studio/actions/runs/41024997190
- Commit: `5ae7bc54b8de761edf77f9e84a55cba0f16470a0`
- Build outputs: workflow artifact `star-sprout-sprint-build` (debug APK + unsigned release AAB).
- Current config: compile SDK 37, target SDK 36, min SDK 24, version 1 / 1.0.0.
- Latest CI run 41050606144: PASS — four JVM unit tests, debug APK, unsigned release AAB, emulator instrumentation, and artifact upload. Artifact ID `12975146658`.
- Monetization build run 41054618157: JVM tests and APK/AAB assembly PASS; emulator test was IN PROGRESS at last check. A follow-up configuration-hardening commit is now triggering CI; verify its run before treating it as green.
- Earlier diagnostic run 41048622797 failed only because the test passed a null accessibility label to `contentEquals`; fixed and verified in 41050606144.
- Physical device, 60 fps, low-memory, battery, AdMob fill, Billing, Play Console, signing, privacy URL: NOT VERIFIED.

## Open risks
1. No signed AAB until owner creates and stores an upload key and adds GitHub secrets.
2. AdMob consent, production ad units/fill, and Play products/account flows remain owner setup and NOT VERIFIED.
3. Final Data Safety and privacy disclosures must match the configured SDK and release binary.
4. No physical device gameplay, performance, accessibility, or consent QA has been performed.
5. Purchase checks are client-side only; server-side Play Developer API verification remains recommended before valuable consumables ship.
6. App uses Java and a custom Canvas View; Kotlin/Flutter migration remains an architecture mismatch with the original request.
7. No hosted privacy-policy URL or Play Console listing exists; release AAB remains unsigned without the owner key.

## Current checkpoint
- Implemented: original playable runner prototype; three biomes/outfits; local saves; original synthesized audio; settings, accessibility actions, and haptics; product/UX/architecture/store drafts; CI unit and emulator tests; consent-gated AdMob with official debug test IDs; optional rewarded diamond grants; interstitial cooldown; Play Billing purchase/restore and local RSA signature checks; privacy and Data Safety drafts.
- Not completed: the current monetization CI run, all remaining Stage 7–11 work, signed release AAB, actual AdMob/Play product setup, server-side purchase verification, physical-device checks, hosted policy URL, and Play Console submissions.

## Owner actions still required
- Create/maintain Play Console and AdMob accounts; provide publisher contact details through the listing, not source code.
- Choose whether to proceed with Kotlin migration (recommended for matching the stated stack) before substantial architecture work.
- Generate upload key locally and add GitHub Secrets; never send private key or passwords in chat.
- Host privacy policy with GitHub Pages and verify exact URL.
- Test on physical Android phones; perform Play Console declarations and recruit closed testers if required for the account.

