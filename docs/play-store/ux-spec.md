# UX specification — Star Sprout Sprint

## Flow
Launch → first-run controls card → home → (world select / outfit room / settings / privacy) → run → pause → resume or home; run ends → results → replay or home. Shop is a planned screen for optional rewarded ad and Play Billing; purchases are parent-free for the chosen 13+ audience, but external links remain safely separated and no external links are currently present.

## Layout wireframes (portrait)

### First run
- Top: original sprout courier and short welcome.
- Middle: four illustrated gestures (left/right lanes, up jump, down slide).
- Bottom: large “Let’s run” button and “Controls” accessible labels.

### Home
- Header: game title, locally saved seed balance.
- Center: animated sprout and selected biome preview.
- Main: primary Play button; secondary Worlds / Outfits; small Settings and Privacy controls.
- Footer: best distance and sound state.

### Worlds / outfits
- One vertically scrollable card per item with original preview, name, lock requirement, selected/owned state.
- Clear earned-seed cost before action; no deceptive countdown or scarcity.
- Back control fixed and at least 48 dp.

### Gameplay
- Full-screen path with strong lane contrast; distance and seed count at top; pause at top right.
- Obstacle/collectible distinction uses shape and motion, not color alone.
- Gesture tutorial overlays only at first run and can be reopened.

### Pause / game over
- Pause: Resume (primary), Restart, Home.
- Results: distance, seeds earned, best marker; optional “double seeds” rewarded-ad offer only if an ad is loaded; Replay and Home. Never imply an ad is required.

### Shop (planned)
- Clear sections: earned-seed cosmetics, optional Play purchase products, optional reward ad.
- Each paid item shows exact localized price from Play and purchase confirmation is handled by Google Play.
- No dark patterns; purchase/restore/support links separated from game controls.

### Settings / privacy / parental gate
- Sound, haptics, motion preference (when implemented), tutorial replay, privacy policy, support.
- 13+ general audience: no child-directed ad flow or parent gate required by the declared target; if audience changes, pause monetization and redesign gates before release.

## Accessibility and interaction rules
- Minimum actionable touch area 48×48 dp; generous spacing; don't rely on fast repeated taps.
- High text/background contrast; scalable text; explicit content descriptions; TalkBack order; haptics/audio can be disabled.
- Respect Android system back and lifecycle; pause on background; keep core gameplay available offline.
- Current Canvas UI has not yet been measured against these targets: NOT VERIFIED.
