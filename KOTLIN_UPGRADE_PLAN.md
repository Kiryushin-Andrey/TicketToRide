# Kotlin Upgrade Plan

## Current state

- Kotlin **1.8.10** (multiplatform: JVM `server`/`common`, JS/IR `client`)
- Gradle **7.6**
- `kotlin-wrappers` **1.0.0-pre.508** — the old pre-release versioning scheme (React/MUI/emotion wrappers), hard-pinned
- Ktor **1.6.4** (server only — EOL for years)
- kotlinx.serialization 1.5.0, kotlinx.coroutines 1.6.4, kotest 5.5.5, shadow plugin (`johnrengelman`) 7.0.0
- No CI config, no `package.json` — Gradle's Kotlin/JS plugin manages Node/Yarn itself, which simplifies things.

Latest stable Kotlin today is **2.4.21**. Jumping there in one shot is risky for this repo specifically because of two coupled dependencies, not the compiler itself:

1. **`kotlin-wrappers`** went from the `1.0.0-pre.NNN` scheme to calendar versioning (`2024.x`/`2025.x`/`2026.x`), and along the way restructured the MUI/emotion/icons artifacts. Moving Kotlin forward far enough eventually forces a wrapper bump, which is a real API migration in `client`, not just a version bump.
2. **Gradle compatibility** is a moving floor: 1.9.x needs Gradle ≥7.6.3-ish, 2.0–2.2 want Gradle up to 8.14, 2.3+ wants Gradle 9.x. Multiple wrapper bumps are needed along the way.

Ktor 1.6.4 isn't a hard blocker for a Kotlin bump (JVM binary compat is strong), but it's old enough to flag as a follow-up, not do in the same change.

## Proposed staged plan

| Stage | Kotlin | Gradle | Notes |
|---|---|---|---|
| 1 | 1.8.10 → **1.9.25** (last 1.9.x) | 7.6 → 8.x | Bump `kotlinx-coroutines` (1.7.x), `kotlinx-serialization` (1.6.x), swap `com.github.johnrengelman.shadow` → `com.gradleup.shadow` (old plugin is unmaintained and breaks on newer Gradle). Verify `kotlin-wrappers:1.0.0-pre.508` still resolves/compiles. |
| 2 | 1.9.25 → **2.0.x** | keep/bump as needed | K2 becomes the default compiler (including for JS) — the main place regressions could appear. Build both JVM and JS, run `server`/`common` tests. |
| 3 | 2.0.x → **2.1.x / 2.2.x** | → 8.14 | Mostly mechanical if stage 2 passed clean. |
| 4 (optional, larger) | → latest 2.4.x | → Gradle 9.x | Only worth doing together with a `kotlin-wrappers` migration to the calendar-versioned releases, since the old pre-release wrapper line won't track that far. This touches React/MUI/emotion usage in `client`. |

Each stage: bump the version(s), run `./gradlew build` for all three modules (JVM + JS/webpack), fix fallout, commit, move on — so a bad interaction is easy to bisect.

## Recommendation

Do stages 1–3 now (gets to a current, well-supported Kotlin 2.x with no client API changes), and treat the `kotlin-wrappers`/MUI modernization (stage 4) plus the Ktor 1→2/3 upgrade as separate follow-up efforts, since both are nontrivial migrations in their own right rather than "upgrade a number."
