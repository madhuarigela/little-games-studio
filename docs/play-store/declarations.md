# Draft Play Console declarations — verify against final binary

The developer is responsible for the final answers. Google may change SDK handling and questionnaire requirements; verify in Play Console before submitting.

## Data Safety

- The monetized build includes Google Mobile Ads and UMP. Google's SDK may collect/share device or advertising identifiers, ad interaction/request details, diagnostics, and fraud-prevention signals. Confirm exact data types, purposes, and sharing from the current SDK disclosure and actual AdMob configuration.
- Google Play Billing processes purchase transactions. The app stores purchased entitlements and purchase-token deduplication records locally. No game account, app server, analytics, or crash-reporting integration is configured.
- Security: app game data is local; Google SDK network traffic uses the vendors' services. Client-side purchase signature verification is weaker than server-side validation.
- Deletion: clear app storage/uninstall for local game state; Google Play account purchase history is handled by Google.
- Host this policy publicly, add real developer contact details, and answer every Data Safety question based on the released AAB and all SDKs.
- Status: draft only. The Console answers are NOT VERIFIED until completed against the exact release binary.

## Content rating questionnaire draft

- Violence: none / very mild cartoon collision only.
- Sexual content, nudity, profanity, drugs, gambling: none.
- User-generated content, chat, location sharing: none.
- Ads and purchases: declare ads and optional in-app purchases present.
- The rating result must come from the real Console questionnaire; it is NOT VERIFIED.

## Ads declaration

- Ads are present in the monetized build via Google Mobile Ads; UMP obtains the current consent status before ad requests.
- Rewarded video is optional and grants three diamonds. Interstitials appear only after a completed run and are capped to one per 180 seconds.
- Production ad units, consent messages, privacy-options configuration, and AdMob account verification require owner setup and real-device checks.

## Target audience

- General audience, 13+ (owner instruction). Marketing and store graphics must not target children.
- Reassess ad treatment, purchase flow, and Families rules before changing the age target.

