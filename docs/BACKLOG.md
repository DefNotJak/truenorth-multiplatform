# Backlog

Work agreed but not started, newest first. Items here are committed to, not
ideas. Speculative things do not belong in this file.

See `docs/DECISIONS.md` for why existing things are the way they are, and
`docs/SPECIFICATION.md` for how the app currently behaves.

---

## Priority 1: do these as soon as the app is live

Raised 2026-10-31. The user asked for all three to be first in line after
launch.

### 1. Test history must survive a change of phone

**Status:** planned in detail, not started. See
`docs/plan-cloud-sync-test-history.md`.

`test_attempts`, `category_stats` and `question_results` live in Room on the
device. A user who changes phone keeps their pass, favourites, flashcard
progress and test date, but loses their **test history, the progress chart, the
category breakdown, and Smart Review**.

**Smart Review is a paid feature.** Someone pays $14.99, upgrades their phone,
signs in, and finds it empty with no explanation. The most reasonable conclusion
available to them is that the app lost their data.

Estimated a focused day, plus a second pass for two-device and offline testing.

### 2. The keyboard covers the sign-up form

**Confirmed in the code, not just reported.** `AuthScreen` has
`verticalScroll(rememberScrollState())` but **`imePadding()` appears nowhere in
the entire app**. A grep across `shared/src` returns no match for `imePadding`
or `WindowInsets.ime`.

Without it the scrollable column has no idea the keyboard exists, so the bottom
of the form sits underneath it and scrolling cannot bring it into view. Reported
symptoms match exactly:

- Part of the Confirm Password field and the **Create Account** button are
  hidden behind the keyboard
- On the sign-in screen the **"Don't have an account? Sign Up"** link is hidden,
  so a new user cannot find the way to register
- The user has to dismiss the keyboard to reach the submit button

**That last one is the serious part.** A new user on the sign-in screen cannot
see the link to create an account. For an app where sign-in is mandatory, that
is a first-run dead end.

**Likely fix:** add `.imePadding()` to the scrollable column in `AuthScreen`,
and audit every other screen with a text field (`ReportQuestionDialog`,
`SettingsScreen` feedback) for the same omission. Consider setting
`contentWindowInsets` on the `Scaffold` rather than patching screen by screen.

Small change, high impact, needs a device to verify.

### 3. Cursor movement by dragging on the keyboard

On iOS, long-pressing the keyboard turns it into a trackpad for moving the text
cursor. It does not work in this app, so a user cannot reposition the cursor
inside a long entry without tapping precisely.

**Honest assessment: this may not be fixable by us.** Compose Multiplatform
renders its own text fields rather than using UIKit's, so system text-editing
gestures are only available to the extent the framework implements them. This
needs checking against the current Compose Multiplatform version before any
promise is made, and the answer may be "wait for upstream".

Lower priority than the two above, and the only one of the three where the
outcome is uncertain.

---

## Unscheduled

- **CI.** A GitHub Actions workflow running `compileCommonMainKotlinMetadata`,
  `compileKotlinIosSimulatorArm64` and `check` on every push. Free, because the
  repository is public. The argument for it is specific: the first iOS compile
  in September surfaced three bugs that had been sitting in the repo for weeks
  and compiled cleanly on Android.
- **Dependabot alerts.** Verify they are on under Settings → Code security.
  **Do not enable version-update PRs**: Kotlin, Compose Multiplatform and AGP
  are tightly version-locked here, so the PRs would break the build and get
  ignored.
- **Play listing says "580+", iOS says 589.** Accurate but inconsistent. Worth
  aligning next time the Play listing is edited.
- **Refresh in-app purchase prices on resume.** Prices are fetched once in
  `LaunchedEffect(Unit)` and never again, so a failed or stale first fetch
  persists for the whole session.
- **Em dashes in shipped copy.** Hundreds across question explanations and the
  Info screen. The owner's no-em-dash rule was stated for written output to him;
  whether it extends to app copy has never been decided. Ask before touching
  content at that scale.
