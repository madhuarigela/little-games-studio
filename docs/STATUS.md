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
| 6 Monetization | NOT STARTED | Requires ad/billing integration, consent review, and purchase test. |
| 7 QA | IN PROGRESS | Four JVM unit tests and APK/AAB assembly pass. Emulator UI test exposed and fixed a null-label bug in the test itself (run 41048622797); rerun pending. Gameplay/device QA remains NOT VERIFIED. |
| 8 Security + compliance | IN PROGRESS | Draft privacy and disclosure materials; must be updated after SDK integration and verified in Console. |
| 9 Release engineering | IN PROGRESS | CI emits debug APK and unsigned AAB. Conditional signing workflow and secrets instructions remain. |
| 10 Store pack | DRAFT | Listing, graphics specs, test plan are in `docs/play-store/`. |
| 11 Post-launch | DRAFT | Metrics plan is in `docs/play-store/post-launch.md`. |

## Green baseline
- Workflow: https://github.com/madhuarigela/little-games-studio/actions/runs/41024997190
- Commit: `5ae7bc54b8de761edf77f9e84a55cba0f16470a0`
- Build outputs: workflow artifact `star-sprout-sprint-build` (debug APK + unsigned release AAB).
- Current config: compile SDK 37, target SDK 36, min SDK 23, version 1 / 1.0.0.
- Automated test results: NOT VERIFIED (baseline workflow does not yet execute test tasks).
- Latest CI run 41048622797: JVM unit tests and APK/AAB assembly PASS; emulator test FAILED due to a null-label bug in the test assertion. Fix is committed locally and awaiting CI rerun.
- Physical device, 60 fps, low-memory, battery, AdMob fill, Billing, Play Console, signing, privacy URL: NOT VERIFIED.

## Open risks
1. No signed AAB until owner creates and stores an upload key and adds GitHub secrets.
2. AdMob and Billing SDKs are not integrated; no revenue is enabled.
3. Adding ad/measurement SDKs changes Data Safety and privacy disclosures; no final declarations until implementation is fixed.
4. A real Android device has not been used for gameplay/performance/accessibility QA.
5. No server-side purchase validation or Play Developer API setup exists; consumables must not ship with client-only verification.
6. App uses Java and a custom Canvas View; the owner asked for Kotlin or Flutter, so this remains an architecture mismatch until migrated.
7. No hosted privacy-policy URL or Play Console listing exists.

## Owner actions still required
- Create/maintain Play Console and AdMob accounts; provide publisher contact details through the listing, not source code.
- Choose whether to proceed with Kotlin migration (recommended for matching the stated stack) before substantial architecture work.
- Generate upload key locally and add GitHub Secrets; never send private key or passwords in chat.
- Host privacy policy with GitHub Pages and verify exact URL.
- Test on physical Android phones; perform Play Console declarations and recruit closed testers if required for the account.

