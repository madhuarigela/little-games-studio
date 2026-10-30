# Draft Play Console declarations — verify against final binary

These are working drafts only. The developer is legally responsible for accurate Console answers after SDKs and behavior are finalized.

## Data Safety (current offline prototype only)
- Data collected: No, based on current manifest/source (no internet permission, analytics, accounts, or ads in baseline).
- Data shared: No, based on current baseline.
- Security practices: game preferences and score stored locally in app-private SharedPreferences; no transport.
- Deletion: clear app storage or uninstall.
- Privacy policy: host `privacy-policy.md` on GitHub Pages and paste exact public URL.
- Revisit immediately if AdMob, Play Billing, crash reporting, analytics, cloud saves, or other SDKs are integrated. SDK collection counts even if the app does not directly collect it.

## Content rating questionnaire draft
- Violence: none / very mild cartoon collision only.
- Sexual content, nudity, profanity, drugs, gambling: none.
- User-generated content, chat, location sharing: none.
- Purchases/ads: answer yes if those features ship; questionnaire answers are NOT VERIFIED until Console flow is completed.
- Rating result must come from the actual questionnaire; do not claim a rating in advance.

## Ads declaration draft
- Current baseline: no ads in binary.
- If monetization stage ships: declare ads present; use Google Mobile Ads SDK, explicit optional rewarded ads, and interstitial only at run results with a 180-second minimum interval. Production ad units require owner account and consent configuration.
- General audience 13+ is selected. Do not select child-directed treatment unless product audience changes; reassess families policy and mixed-audience rules if children become a target group.

## Target audience
- General audience, 13+ (owner instruction).
- Confirm actual target age bands and content in Play Console. Marketing must not target children. If this changes, stop and redesign monetization/compliance before release.
