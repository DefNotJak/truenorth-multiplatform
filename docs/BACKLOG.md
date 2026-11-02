# Backlog

Work agreed but not started, newest first. Items here are committed to, not
ideas. Speculative things do not belong in this file.

See `docs/DECISIONS.md` for why existing things are the way they are, and
`docs/SPECIFICATION.md` for how the app currently behaves.

---

## Priority 1: do these as soon as the app is live

Raised 2026-10-31. The user asked for all three to be first in line after
launch.

### 1. Test history must survive a change of phone - DONE 2026-11-01

Implemented per `docs/plan-cloud-sync-test-history.md`. Compiles and unit tests
pass on all three targets. **Two things stand between this and working:** the
Firestore rule has to be deployed, and the Room migration has to be run on a
real upgrade. Neither can be done from here. See "Before this ships" below.

Room stays the source of truth for every read; Firestore is a mirror. No screen
changed.

- `test_attempts.syncedAtMillis`, nullable, marks what still needs uploading.
  Every pre-existing row gets null, which is what carries an existing user's
  local-only history up on first launch instead of stranding it.
- `sync_state`, a one-row table holding the uid the local database belongs to.
- Room 1 to 2 migration written by hand. `fallbackToDestructiveMigration` would
  have been one line and would have wiped the history this feature exists to
  save, on every install, at the moment of upgrade. `exportSchema` is now on and
  `shared/schemas/...AppDatabase/2.json` is checked in.
- `TestHistorySync.kt` holds the pure logic: deterministic attempt ids, the
  union merge, the missed-set filter. 17 tests in `commonTest`.
- `TestHistorySyncRepository` does the Firestore work: upload, restore, the
  missed-set mirror, and cloud deletion on account delete.
- Wired in `AppNavHost`: restore once per `userId`, upload after each saved
  test, upload again on resume. All fire-and-forget inside try/catch, because a
  sync failure must never touch a practice test.

Three hazards found while building it that the plan had not anticipated:

1. **Two accounts on one device.** Room is per-device and signing out does not
   clear it, so `uploadPending()` would have pushed the first person's attempts
   into the second person's account, permanently. `sync_state.ownerUid` now
   clears local history when the signed-in uid changes. The trade-off is real
   and accepted: attempts the first person never managed to sync are destroyed
   rather than contaminating another account. They cannot be rescued first,
   because the rules correctly refuse to let the second person write to the
   first person's document.
2. **The missed-set write is destructive.** `syncMissedQuestionIds()` replaces
   the whole array, because answering a question correctly has to remove it. On
   a new device whose restore had just failed on a bad network, the resume path
   would have pushed an empty array over the real set and emptied Smart Review
   for the account for good. It is now guarded behind a successful restore.
   Uploading attempts needs no guard, since it only ever adds documents.
3. **Firestore does not cascade.** Deleting `users/{uid}` leaves its `attempts`
   subcollection in place, reachable and billable, with nothing above it to show
   it exists. Account deletion would have kept the test history of someone who
   asked to be forgotten. `deleteAllCloudHistory()` now runs first, while the
   user is still signed in and the rules still authorise it.

#### Before this ships

**1. Deploy the Firestore rule.** `firestore.rules` now contains a
`users/{userId}/attempts/{attemptId}` block that exists **only in that file**.
Nested match blocks do not inherit the parent's allow, so until it is pasted
into the Firebase Console and published, every sync write is denied. It fails
silently in the background: no crash, no message, nothing the user could report.
The only symptom is history never appearing on a second device, which looks
exactly like the bug this was built to fix.

**2. Test the Room migration on a real upgrade.** Install the current store
build, take a test so there is history, then install this build over it. The
migration SQL is character-identical to what Room generates for the entities,
checked against the exported schema, but "identical to the generated statement"
is not the same as "ran successfully on a device". If it is wrong the app
crashes on open for every existing user, which makes this the highest-stakes
item in the change.

**3. The rest of the manual list**, from the plan: force-quit before sync and
confirm it uploads later; sign in on a second device; take tests on both and
confirm the union; aeroplane mode; and an existing user with local-only history
whose first launch must upload it rather than wipe it.

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
