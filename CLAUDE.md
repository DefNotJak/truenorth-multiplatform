# Working agreements for this repository

Read this before doing anything in this project.

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
