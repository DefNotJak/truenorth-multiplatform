# Plan: sync test history to the cloud

**Status:** not started. Written 2026-10-18.

## The problem, stated precisely

Half of a user's account lives in Firestore and half lives in a Room database on
the device. Nothing about the app explains which is which, so the split is
invisible until it bites.

| Survives a phone change | Does not |
|---|---|
| Pass and expiry | Test history |
| Favourite questions | Progress chart |
| Flashcard progress | Category breakdown |
| Test date countdown | **Smart Review** |
| Reported questions | |

**Smart Review is a paid feature.** Someone pays $14.99, upgrades their phone,
signs back in, and finds their pass intact but Smart Review showing zero
questions. There is no message, no explanation, and nothing they can do. The
most reasonable conclusion available to them is that the app lost their data.

The progress chart is also a headline selling point ("watch your scores improve
over time") that silently resets.

This has probably not hurt anyone yet, because the install base is tiny. It will.

## What success looks like

Sign in on a new device, and your history, your progress charts and your Smart
Review queue are all there. Nothing else about the app changes.

---

## Design

### Where the data goes

Keep the three Room tables as the **local source of truth for reads**, and mirror
to Firestore for durability and cross-device restore. Do not replace Room with
Firestore outright.

Reasons for mirroring rather than migrating:

- Room gives instant, synchronous, offline reads. The Progress screen and the
  Home stats row read on every composition, and routing those through Firestore
  listeners risks visible flicker on cold start
- Firestore offline persistence is good but behaves differently, and swapping it
  in wholesale means re-testing every screen that touches history
- A mirror can be added incrementally and reverted cleanly. A migration cannot

### Firestore shape

Two additions to the existing `users/{uid}` document, plus one subcollection.

**On the user document:**

| Field | Type | Why here |
|---|---|---|
| `missedQuestionIds` | List of Int | Bounded at 589. Small, read on every Home render, wants to arrive with the rest of the user doc rather than as a separate query |
| `historySchemaVersion` | Int | So a future shape change can be detected and migrated rather than guessed at |

**Subcollection `users/{uid}/attempts/{attemptId}`:**

| Field | Type |
|---|---|
| `timestampMillis` | Long |
| `correctCount` | Int |
| `totalQuestions` | Int |
| `categoryStats` | Map of category name to `{correct, total}` |

Category stats are **denormalised onto the attempt** rather than given their own
subcollection. They are only ever read together with their attempt, they are
bounded at six entries, and one document per attempt keeps both the read count
and the write count at exactly one.

`attemptId` is a **deterministic string derived from the local row**, not a
Firestore auto-id. See idempotency below.

### Why not just put attempts on the user document

An array of attempts on the user doc would be simpler, but Firestore documents
cap at 1 MiB and a committed studier could plausibly take hundreds of tests.
More importantly, every write would rewrite the whole array, which races against
the existing `grantPass` and `incrementFreeTestsUsed` transactions on the same
document. A subcollection keeps attempt writes entirely off the hot document.

### Idempotency

Mirroring must be safe to repeat. The device may upload, lose connectivity
before recording success, and retry.

Give every attempt a stable id at creation time:

```
attemptId = "{timestampMillis}-{correctCount}-{totalQuestions}"
```

Writing the same attempt twice then overwrites one document instead of creating
a duplicate that inflates the user's test count. Add a local `syncedAt` column to
`test_attempts` so the uploader knows what still needs sending.

---

## Work, in order

### 1. Schema change, Room version 1 to 2

Add a nullable `syncedAtMillis` column to `test_attempts`. Supply an explicit
`Migration` rather than `fallbackToDestructiveMigration`, which would wipe the
very history this project exists to preserve.

`AppDatabase` currently declares `version = 1` and `exportSchema = false`.
**Turn schema export on** in the same change, so future migrations can be
validated against a checked-in schema instead of reasoned about.

New DAO methods: `observeUnsyncedAttempts()`, `markAttemptSynced(id, millis)`,
and bulk inserts for the restore path.

### 2. `TestHistorySyncRepository`

New class in `commonMain`, constructed with the uid like the other Firestore
repositories.

```
suspend fun uploadPending()           // push unsynced local attempts
suspend fun restoreFromCloud()        // pull everything, merge into Room
suspend fun syncMissedQuestionIds()   // push the derived set to the user doc
```

`uploadPending` walks unsynced attempts, writes each as one document with its
deterministic id, and marks it synced. A failure leaves the row unsynced and the
next run retries it; that is the whole error-handling strategy and it is
sufficient.

`restoreFromCloud` reads the subcollection and inserts anything whose id is not
already present locally. **Merge, never replace.** A user with history on two
devices should end up with the union, not whichever device synced last.

### 3. Rebuilding `question_results` on restore

This is the subtle one, and the reason `missedQuestionIds` is stored separately
rather than derived.

`question_results` holds **only the most recent outcome per question**, and
attempt documents do not record per-question results. So a fresh device cannot
reconstruct it from attempts alone.

Two options:

**Option A, chosen.** Mirror the derived `missedQuestionIds` set to the user
document whenever it changes, and on restore write those ids into
`question_results` as `wasCorrect = false` with `lastAttemptMillis` set to the
restore time. Questions the user got right are simply absent, which is exactly
how the table already behaves. Smart Review works immediately; the only thing
lost is the original timestamp, which nothing reads.

**Option B, rejected.** Store full per-question results on each attempt. Exact,
but multiplies document size by the question count, and nothing in the app uses
per-question history beyond the missed set.

### 4. Wiring

In `AppNavHost`, where the other repositories are already created per `userId`:

- On first composition for a user, call `restoreFromCloud()` **once**, guarded by
  a flag so it does not rerun on every recomposition
- After `TestResultsRepository.saveAttempt()` succeeds, fire `uploadPending()`
  and `syncMissedQuestionIds()`
- On `ON_RESUME`, call `uploadPending()` to catch anything a kill or a dead
  network left behind. The billing reconciliation already uses
  `LifecycleResumeEffect` here, so follow that pattern

All of it fire-and-forget on a coroutine scope. **A sync failure must never
block or break a practice test.** Local writes stay the source of truth for the
session.

### 5. Firestore security rules

The subcollection needs rules. Today's rules cover `users/{uid}` and
`questionReports`; `users/{uid}/attempts/{id}` will be denied by default unless
explicitly allowed. Mirror the existing user-document rule: a signed-in user may
read and write only their own subtree.

**Check this before shipping.** A missing rule fails silently in the background
and the symptom is simply that nothing ever syncs.

---

## Testing

Pure logic goes in `commonTest`, which is where every existing test lives:

- Deterministic attempt id generation, including collisions between two attempts
  in the same millisecond with identical scores
- Merge logic: local-only, cloud-only, and overlapping sets all produce the union
- `missedQuestionIds` round-trips to `question_results` correctly

Manual checks that no unit test will cover:

1. Take a test, force-quit before sync, reopen, confirm it uploads
2. Sign in on a second device, confirm history and Smart Review appear
3. Take tests on both devices, confirm both end up with everything
4. Aeroplane mode: take a test, confirm it works normally and syncs on reconnect
5. Existing user with local-only history: confirm their first launch uploads it
   rather than wiping it

Point 5 is the one that would hurt most if it went wrong.

---

## Scope boundaries

**In scope:** attempts, category stats, the missed-question set, and restore on a
new device.

**Out of scope, deliberately:**

- Real-time sync between two devices in simultaneous use. Sync on save and on
  resume is enough
- Conflict resolution beyond union-merge. Attempts are append-only facts, so
  there is nothing to conflict
- Moving theme, last-used test config or the review-prompt flag to the cloud.
  Those are genuinely per-device
- Server-side validation. Still no backend, same accepted trust model

---

## Estimate

A focused day of implementation, plus a second pass for the two-device and
offline testing, which cannot be rushed and cannot be automated here.

## When

**After both store releases have landed.** There is no reason to disturb an app
sitting in review.

**Before any marketing push.** Every user acquired before this ships is someone
who can hit a silent, unexplained data loss on a device change, and that is the
kind of thing that produces one-star reviews which cannot be answered.
