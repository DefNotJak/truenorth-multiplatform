# Working agreements for this repository

Read this before doing anything in this project.

## How to give the owner instructions

**The owner is a beginner at building and shipping apps, and is not a
developer.** Every instruction that involves a tool, a console, a website or a
terminal must be written as numbered steps he can follow without already knowing
how the thing works. This is a standing instruction, given more than once, in
his words:

> "I need you to be much more instructive - give me exact steps where to put
> each phase, where to click, how to get there."

Treat vague guidance as a defect in the answer, not as something he should ask
to have expanded.

What that means in practice:

- **Name the destination and the route.** Not "open the Firebase Console", but
  which site, signed in with which account, and which project to pick from the
  list.
- **One action per numbered step.** If a step contains "and then", split it.
- **Quote the literal text** on the button, tab or link, in the capitalisation
  it actually uses. He is looking for that exact word on screen.
- **Say what he should see after each step.** A wrong turn then shows up
  immediately instead of three steps later.
- **Never say "go to settings" or "configure X".** Say which Settings, reached
  from where, and what to change once there.
- **Give exact text to paste** whenever something has to be typed, including
  commands, and say which window to paste it into.
- **Warn before an irreversible step, not after it.**
- **Assume no knowledge of conventions** in git, Gradle, Xcode, Terminal, the
  Firebase Console, Play Console or App Store Connect. Explaining what a button
  does is part of the instruction, not an aside.
- **He works on Windows.** On a rented Mac he does not know where things are, so
  say how to find them, including the Finder and Terminal themselves.

When a task cannot be done from here and has to be done by him in a browser or
on a Mac, the answer is not finished until those steps are written out.

## Before you act

**`docs/SPECIFICATION.md` is the source of truth for how this app behaves.**
Read it before answering any question about behaviour, data storage, pricing,
the question bank, or the free/paid split. Do not reason from memory or
assumption. If the spec and the code disagree, **say so explicitly** rather than
silently picking one.

**`docs/DECISIONS.md` records why things are the way they are.** Check it before
proposing a change to anything structural. Several decisions here look wrong
until you know the constraint behind them.

## After you act

These are not optional, and nobody should have to ask for them.

**Update `docs/SPECIFICATION.md` in the same commit** as any change to:

- the question bank's size, structure, or the meaning of any field
- where a piece of data is stored (Firestore, Room, or platform preferences)
- the free/paid split, pricing, or product IDs
- scoring, timing, or question-selection rules
- the public URLs or contact details in section 12
- anything in section 10, "Known limitations"

**Append to `docs/DECISIONS.md`** whenever a choice is made that a reasonable
person might later reverse without knowing why it was made. One entry, dated,
with the alternative that was rejected. Small is fine; missing is not.

**A stale spec is worse than no spec, because it gets trusted.**

## House style

- **No em dashes.** Standing instruction from the owner, applies everywhere:
  code comments, commit messages, documentation, and chat.
- Commit messages explain *why*, not *what*. The diff already shows what.
- Comments explain the non-obvious constraint, not the mechanics.

## Verification

This is a Kotlin Multiplatform project, so a change can compile on one target
and break another. Before claiming a change works, run all three:

```
./gradlew :shared:compileCommonMainKotlinMetadata
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:check
```

`JAVA_HOME` is not set in this environment. Use:
`/c/Program Files/Android/Android Studio/jbr`

**`:shared:iosSimulatorArm64Test` cannot run.** It fails at link time because the
native Firebase frameworks come from Swift Package Manager inside the Xcode
project and Gradle cannot see them. This is expected. See spec section 10.

## Never commit

- `keystore.properties`, `*.jks`, `*.keystore`, or anything containing a password
- `local.properties` (machine-specific paths)
- API tokens or provisioning profiles

`google-services.json` and `GoogleService-Info.plist` **are** committed, and that
is correct. They contain client configuration that ships inside every copy of
the app; they are not secrets. GitHub's secret scanner flags them anyway. That
alarm is a false positive and has already been investigated once.

## Related repositories

The four consumer-facing web pages live in **`DefNotJak/truenorth-privacy-policy`**,
not here, cloned at `C:\AndroidProjects\truenorth-privacy-policy`. The privacy
policy, terms of service, account deletion and support pages are all there, and
App Store Connect and Play Console both depend on those URLs resolving.

The original native Android app is at `C:\AndroidProjects\TrueNorth Citizenship Test`
and is **still the app live on Google Play**. This KMP project has not replaced
it yet. Icon or content changes must be applied to both until it does.
