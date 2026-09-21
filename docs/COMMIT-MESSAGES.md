# Suggested commits

No commit or push was performed. Review each independent repository first; earlier uncommitted language/menu changes are also present.

## Android — Tipspromenad-app-for-Android

```text
feat: add offline quiz creation, portable sharing and result collection

Bundle the quiz engine and versioned bank in a restricted local WebView.
Add seeded quiz creation, portable quiz/result QR codes, participant names,
local leaderboards, time walking and foreground distance walking.
Preserve classic quizzes, reuse saved settings from the overflow menu,
add a four-step creator wizard and modernize the home menu.
Add Back/Quit handling and the Nightfall Run-style publication notice.
Keep participant data on-device and exclude cloud/device-transfer backup.
Add emulator workflow tests, screenshots and privacy/format documentation.
```

## Web — TipspromenadQuizWebPage

```text
feat: support portable quiz joining and offline participant answer sheets

Add TIPQ/TIPR binary codecs, deterministic 2/3/4-option rendering,
named attempts, QR scanning, portable result codes and local storage.
Share creator/scoring logic with Android while keeping the public website
participant-focused and preserving classic quizzes.
Add offline caching, six-language UI and format/compatibility tests.
Modernize navigation, keep Random Quiz configuration in Settings,
and use the custom source publication notice.
```

## Questions — Tipspromenad

```text
refactor: make Tipspromenad a versioned question-data repository

Replace the active webpack app with canonical schema-v2 question data.
Assign stable 24-bit IDs, four options per translation, category metadata
and an immutable revision with integrity and ID-registry checks.
Preserve legacy provenance and flag ambiguous questions for review.
Document the local legacy-app archive, provenance and privacy.
Adopt the Nightfall Run-style source publication notice while preserving
third-party rights and earlier license grants.
```

## Follow-up: menu and inline correction

Android: `feat: simplify menu and show random quiz corrections inline`

Web: `feat: remember participant names and correct random quizzes in place`

Question database: no changes in this follow-up; no new commit needed.

## Modern question layout

Android: `feat: add 1-X-2 answer cards and full-height single-question navigation`

Web: `feat: modernize quiz cards, creator count and walking navigation`

Question database: unchanged.
## Current changes — 2026-09-21

### Android
```text
feat: add phone-hosted LAN quizzes, persistent gates and tie-breakers

Host experimental local quizzes directly from Android using a foreground
service. Serve quiz data to participants and collect scored results locally.
Persist time/distance gates and require both when configured together.
Add numerical tie-breakers, compatible v2 share codes and 17-language
selection with English fallback. Bundle revision 2 and add host tests.
```

### TipspromenadQuizWebPage
```text
feat: align WebQuiz with Android and support local-host participation

Add creator and organizer flows, difficulty filters and numerical tie-breaks.
Match shared navigation, question cards, progress and inline corrections.
Keep timed gates while gracefully skipping browser distance requirements.
Join Android-hosted LAN rooms, receive exact quiz data and retry results.
Add versioned sharing, database refresh and multilingual fallbacks.
```

### Tipspromenad question database
```text
feat: publish revision 2 with expanded questions and numeric tie-breakers

Add 48 normal questions and two numerical tie-breakers with stable IDs.
Extend starter and tie-breaker translations to 17 language codes.
Preserve revision 1, add question sets and difficulty metadata, and update
schema, registry, integrity manifest and revision validation tests.
```

These are suggested messages only; no commits or pushes were performed.
