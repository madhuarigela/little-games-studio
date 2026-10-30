# Post-launch measurement plan

## Privacy rule
Do not introduce analytics, identifiers, precise location, contacts, or ad attribution without documented necessity, consent/policy review, and updated disclosures. Prefer aggregate Play Console reports. Do not promise “anonymous” unless implementation proves it.

## Metrics to review weekly
- Day-1 retention and returning users (Play Console aggregate).
- Sessions per active user and median session length.
- Tutorial completion and early-run exit points (only if event measurement is approved and disclosed).
- Crash-free users, crash rate, ANRs, startup time, and frame pacing.
- Rewarded-ad opt-in/completion and revenue per active user after production integration; never optimize by pressuring players.
- Purchase conversion/refund/chargeback and entitlement reconciliation.

## Improvement loop
1. Compare cohorts by app version and device class using aggregate dashboards.
2. Prioritize crashes, control confusion, crashes/ANRs, heat/battery complaints, and accessibility defects.
3. Change one gameplay/economy parameter at a time; use a documented experiment plan.
4. Recheck privacy, ads, age targeting, and store declarations for every SDK or behavior change.
5. Roll back if crashes, complaints, deceptive monetization patterns, or policy discrepancies increase.

Analytics integration is NOT VERIFIED and is intentionally not enabled in the current offline baseline.
