## Current shared quiz features

Android and WebQuiz support quiz creation, manual/random category selection, difficulty filters, four answer alternatives, participant names and portable quiz/result codes. Numerical tie-breakers appear after normal questions and rank equal scores by absolute difference; they do not increase the normal score.

Progress gates are optional. Android requires both elapsed time and distance when both are enabled. Next stays disabled until the gate is satisfied and an answer is selected; readiness does not automatically advance. Attempts and gate progress persist locally. GPS measures foreground movement. WebQuiz ignores distance requirements and retains time requirements, with a visible explanation.

Experimental local sharing uses the **Android phone as the host**. Start hosting from Experimental Wi-Fi sharing, choose a saved quiz, and share the displayed local address and short room code. Participants open that address on the same Wi-Fi/hotspot. The phone serves the quiz database and collects results locally; no computer or cloud participant service is required. Keep the host service running (visible notification). Results that cannot be sent are saved locally and retried while the participant page is open. Network isolation on some Wi-Fi networks can prevent connections. Automatic discovery is not implemented. Physical phone-to-phone Wi-Fi and outdoor GPS still need real-device testing.

There are 17 selectable language codes: en, sv, es, da, no, fi, is, th, zh, ja, ko, de, fr, it, nl, pt, pl. Translation coverage varies: core navigation, six starter questions and two tie-breakers cover all 17; other missing text falls back to English. Classic quizzes retain the original six languages and use English for newly added languages.

Revision 2 contains 78 normal questions and two numerical tie-breakers, including 48 new normal questions. Existing questions remain, including previously deprecated entries. Published revisions must remain immutable. Online clients check the question repository's latest manifest and verified SHA-256; bundled/cached data supports offline use. Portable codes identify the exact revision and question IDs. LAN joining also transfers that revision's bank, so it works without Internet access.

Participant names and answers are stored locally. Experimental LAN sharing explicitly sends them to the organizer's phone on the local network. Public question data and reference answers are not a secure examination/anti-cheating system.
