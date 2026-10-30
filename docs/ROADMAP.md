# Roadmap

1. Select one game for the first playable prototype.
2. Choose a shared Android/game technology after comparing the needs of the selected prototype and the other planned games.
3. Implement one complete, polished gameplay loop and review it on a real Android device.
4. Use feedback to refine the game and decide which shared systems are genuinely useful.
5. Build the remaining games independently under `apps/`.
6. Decide monetization, privacy disclosures, age targeting, and store setup before adding any advertising, analytics, or billing SDKs.

Each app will need its own application ID, icon, signing setup, store listing, and release artifact. Never commit signing keys or private credentials. The first development milestone is a testable prototype, not a production Play Store release.