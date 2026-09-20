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
