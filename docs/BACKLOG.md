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

### 2. The keyboard covers the sign-up form - DONE 2026-11-01

Fixed, pending verification on a physical device.

`AuthScreen` had `verticalScroll(rememberScrollState())` but no `imePadding()`,
and `imePadding` appeared **nowhere in the app**. A scrollable column with no
IME inset has no idea the keyboard exists, so the bottom of the form sat
underneath it and scrolling could not bring it into view. The serious part was
not the Create Account button: on the **sign-in** screen the "Don't have an
account? Sign Up" link was hidden, and sign-in is mandatory, so a new user hit
a first-run dead end.

What changed, all in `AuthScreen.kt` unless noted:

- `.imePadding()` on the scrolling column, between `.padding(padding)` and
  `.verticalScroll(...)`. The Scaffold consumes its own insets, so this adds
  the keyboard height only and does not double-count the navigation bar.
- `android:windowSoftInputMode="adjustResize"` on `MainActivity` in
  `androidApp/src/main/AndroidManifest.xml`. `enableEdgeToEdge()` was already
  being called, which is what actually makes Android dispatch IME insets, but
  the Compose docs ask for `adjustResize` explicitly and OEM skins vary.
- `imeAction` on all three fields, so the keyboard offers a way forward
  instead of a bare return key: Email â Next, Password â Next when signing up
  and Done when signing in, Confirm Password â Done.
- `keyboardActions` moving focus with `LocalFocusManager`, and submitting on
  Done.
- The submit logic moved out of the `Button(onClick = { ... })` lambda into a
  local `submit()`, so the button and the keyboard's Done key run the same
  code instead of two copies. `canSubmit` was extracted for the same reason.
  `submit()` also calls `focusManager.clearFocus()`, which drops the keyboard
  on submit rather than leaving it over the result.

**Audited and deliberately left alone:** the only other text fields in the app
are the note in `ReportQuestionDialog` and the password in the delete-account
re-authentication dialog in `SettingsScreen`. Both sit inside a Material3
`AlertDialog`, which gets its own window with `decorFitsSystemWindows = true`
and is centred in the space the keyboard leaves, so neither needs
`imePadding()`. The re-auth field also deliberately has **no** Done action:
its confirm button is "Confirm & Delete", and a return key should not trigger
account deletion.

Still to do: run it on a physical phone. The behaviour depends on real IME
insets, which the emulator and simulator approximate.

### 3. Cursor movement by dragging on the keyboard - investigated 2026-11-01

**The earlier note in this file was wrong** and is corrected here. It said this
might not be fixable and the answer could be "wait for upstream". That was
written from memory of old Compose Multiplatform bug reports and was not
checked.

What is actually true:

- JetBrains issue
  [#4593](https://github.com/JetBrains/compose-multiplatform/issues/4593) is
  the exact complaint ("iOS 17 hidden trackpad doesn't work in the TextFields").
  It was **closed as completed on 3 June 2024**, via
  compose-multiplatform-core PR #1312, "Initial iOS floating cursor support".
- This project is on Compose Multiplatform **1.11.1**, well past that. Verified
  directly rather than assumed: unpacking
  `ui-iosSimulatorArm64Main-1.11.1.klib` from the Gradle cache shows
  `beginFloatingCursorAtPoint`, `updateFloatingCursorAtPoint`,
  `endFloatingCursor`, `floatingCursorTranslation` and `closestPositionToPoint`
  all present. **The framework has the gesture.**

So the question became why it did not work here, and the answer is that it was
almost certainly tried on a password field.

- The same klib shows Compose's iOS text input view sets `isSecureTextEntry`,
  derived from the field's `KeyboardType`.
- **iOS itself disables the floating cursor in secure text entry.** This is
  Apple's behaviour, not a framework bug. Flutter has the identical report:
  [flutter#122139](https://github.com/flutter/flutter/issues/122139), "Hold and
  drag spacebar does not move cursor when `obscureText` is true".
- Both of `AuthScreen`'s password fields used `KeyboardType.Password`, so both
  were secure, so the gesture was blocked. That is correct while the password
  is hidden.

**Fixed:** the password fields now use `KeyboardType.Text` while
`passwordVisible` is true. Tapping the eye icon already reveals the password on
screen, so there is nothing left for secure text entry to protect, and dropping
the secure flag brings the gesture back. `autoCorrectEnabled = false` and
`capitalization = KeyboardCapitalization.None` went on at the same time, since
a plain text keyboard would otherwise autocorrect and capitalise a password.

**Not fixed, and not ours to fix:** a hidden password field still will not
accept the gesture. Apple blocks it.

Left to check on a device: the gesture in the app's two non-secure fields, the
email field and the "Report an issue" note. If it fails there, that is a real
upstream bug and worth filing against Compose Multiplatform with a version
number. If it works, item 3 is finished.

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
