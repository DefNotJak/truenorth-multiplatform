# TrueNorth: Citizenship Test - Full Specification

**This file is the source of truth for how the app behaves.** It exists so that
nobody, human or AI, has to infer a specification from the code or invent one
from memory. If the code and this document disagree, that is a bug in one of
them, and the disagreement should be resolved explicitly rather than guessed at.

Last verified against the codebase: **2026-10-18**, commit `4d2eaf4`.

Everything below was read out of the source, not recalled.

---

## 1. What the app is

An independent study aid for the Canadian citizenship test. It is **not**
affiliated with the Government of Canada or IRCC, and every store listing and
in-app surface carries that disclaimer. Content is written from, and cites, the
official *Discover Canada* study guide, but reproduces none of its text.

### Store identities

The same app ships under three different names, which is deliberate and an easy
source of confusion:

| Where | Name |
|---|---|
| Internal / codebase / in-app wordmark | **TrueNorth: Citizenship Test** |
| Apple App Store | **Citizenship Test Canada** |
| Google Play | **Canadian Citizenship Test** |

- **Bundle / application ID (both platforms):** `com.truenorth.citizenshiptest`
- **Apple Team ID:** `KVQ37C284U`
- **Firebase project:** `truenorth-citizenship-test`

---

## 2. Platforms and stack

Kotlin Multiplatform with Compose Multiplatform. One shared UI and one shared
business layer; the platform modules are thin shells.

| Layer | Technology |
|---|---|
| UI | Compose Multiplatform (Material 3) |
| Navigation | `org.jetbrains.androidx.navigation:navigation-compose` |
| Auth + cloud data | Firebase via GitLive `dev.gitlive:firebase-auth` / `firebase-firestore` |
| Local database | Room 3 (`androidx.room3`) over bundled SQLite |
| Android billing | Play Billing |
| iOS billing | **StoreKit 1** (`SKPaymentQueue`), because StoreKit 2 is Swift-only and unreachable from Kotlin/Native cinterop |
| Android preferences | AndroidX DataStore |
| iOS preferences | `NSUserDefaults` |

Shared code lives in `shared/src/commonMain`. Platform specifics use
`expect`/`actual`, including `@Composable expect fun rememberX()` factories so
that `commonMain` never touches an Android Context or a UIViewController.

---

## 3. The question bank

Single source: `shared/src/commonMain/.../data/QuestionBank.kt`, roughly 6,250
lines and by far the largest file in the project.

### Totals, counted from source

| Metric | Value |
|---|---|
| **Total questions** | **589** |
| Multiple choice | 460 |
| True/False | 129 |
| Distinct topic groups (max possible test length) | **532** |
| Questions carrying an explicit `topicGroupId` | 357, forming 300 groups |
| Questions with a `flashcardText` override | 42 |

### By category

| Category (enum) | Display name | Questions |
|---|---|---|
| `HISTORY` | Canada's History | 135 |
| `GOVERNMENT` | How Canadians Govern | 132 |
| `ECONOMY_GEOGRAPHY` | Economy & Geography | 96 |
| `WHO_WE_ARE` | Who We Are | 81 |
| `RIGHTS_RESPONSIBILITIES` | Rights & Responsibilities | 78 |
| `SYMBOLS` | Canadian Symbols | 67 |

### Question model

```kotlin
data class Question(
    val id: Int,
    val category: Category,
    val type: QuestionType,          // MULTIPLE_CHOICE | TRUE_FALSE
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val topicGroupId: String = "q{id}",
    val flashcardText: String? = null
)
```

### Two non-obvious fields, both load-bearing

**`topicGroupId`** groups questions that test the *same underlying fact* in
different wordings. Test selection takes **at most one question per group**, so
a single test never asks the same fact twice, and repeat attempts rotate
naturally through phrasings. It defaults to a unique value per question, so a
question with no variants needs no attention. **Only give two questions the same
group when they are genuinely interchangeable.**

**`flashcardText`** is an alternative wording used *only* by Flash Cards.
Questions phrased "Which of the following is..." read fine in a practice test
where all four options are visible, but become meaningless on a flashcard
showing only the correct answer. Practice Test always uses `text` and ignores
this field.

### Selection algorithm

`QuestionBank.customTestSet(count, categories, questionType, restrictToIds)`:

1. Filter the pool by category, type and explicit id restriction
2. Group by `topicGroupId`
3. Shuffle the groups, take `count` of them
4. Pick one question at random from each chosen group

**If fewer than `count` groups match, the test is simply shorter.** Callers must
size their UI off the actual returned list, never off `count`.

`matchingQuestionCount(categories, questionType)` returns the number of distinct
groups matching the filters, i.e. the longest test currently possible.

### Flashcard eligibility

`flashcardEligible(category)` excludes **True/False questions whose correct
answer is "False"**. A card reading "ANSWER: False" teaches nothing and risks
being misremembered as the true statement. Those questions remain perfectly
usable in Practice Test, where all options are shown.

---

## 4. Features, screen by screen

Routes are defined in `navigation/Routes.kt`.

| Route | Screen | Free or paid |
|---|---|---|
| `home` | Home: stats row plus the menu | Free |
| `practice_test_setup` | Configure a test | Free, consumes the daily allowance |
| `practice_test` | The test itself, results, and answer review | Free, consumes the daily allowance |
| `study` | Category picker for Flash Cards | Free |
| `flashcard_deck/{categoryName}` | A flashcard deck | Free |
| `favorite_questions` | Starred questions | **Paid** |
| `smart_review` | Not a real destination, see below | **Paid** |
| `progress` | Scores, category breakdown, chart | Free |
| `info` | How the Real Test Works | Free |
| `settings` | Theme, account, feedback, about | Free |
| `paywall` | Pass comparison and purchase | Free |

**`SMART_REVIEW` is not a NavHost destination.** It exists as a route string
only so it can sit as an ordinary row in the Home menu list; `AppNavHost`
special-cases it and launches a filtered Practice Test session instead of
navigating anywhere.

### Home

A stats row (Tests Taken, Avg Score, Pass Rate, Days Left) over a menu: Practice
Test, Flash Cards, Favourite Questions, Smart Review, Progress, How the Real
Test Works, Settings. Smart Review's subtitle changes to show the count of
questions awaiting review.

### Practice Test

- Default length **20 questions**; the setup screen offers **10, 15, 20, 25, 30**
- Optional filters: categories, question type
- **45-minute timer**, turning a warning colour in the final **5 minutes**
- **Pass mark 75%**, matching the real test
- Feedback is immediate on each answer, not deferred to the end
- Every answer carries an explanation, usually with a *Discover Canada* page citation
- Afterwards: a results summary, a per-category breakdown, and a full answer review

### Scoring

`PASS_THRESHOLD_PERCENT = 75`. `computeHomeStats` returns tests taken, average
score, overall accuracy, and a pass rate computed over the **last 5 attempts**
(`RECENT_TESTS_WINDOW`). Category breakdown is sorted **ascending by percent**,
so the weakest category appears first.

### Smart Review (paid)

Builds a practice test from **only the questions the user most recently got
wrong**, capped at **30 questions** (`SMART_REVIEW_MAX_QUESTIONS`). Entering via
the Home menu while unsubscribed routes to the paywall.

### Flash Cards (free)

Per-category decks. Which cards have been seen is stored per account.

### Favourite Questions (paid)

Starring a question while unsubscribed routes to the paywall rather than
silently failing.

### Settings

Theme mode, test date, subscription status, feedback (a `mailto:` link via
`LocalUriHandler`, not a platform intent), account deletion, and the
non-affiliation disclaimer.

### Dialogs

`OnboardingDialog`, `PrivacyConsentDialog`, `TestDatePickerDialog`,
`ReportQuestionDialog`, and a `SpotlightTutorial` overlay.

---

## 5. Free versus paid

**One free practice test per day** (`DAILY_FREE_TESTS_LIMIT = 1`). The counter is
stored alongside the date it applies to, and any stored date other than today is
treated as zero used, so the allowance resets at local midnight without needing a
scheduled job.

| Feature | Free | Paid |
|---|---|---|
| Practice Tests | 1 per day | Unlimited |
| Flash Cards, all categories | Yes | Yes |
| Progress tracking | Yes | Yes |
| Test date countdown | Yes | Yes |
| How the Real Test Works | Yes | Yes |
| Report a question | Yes | Yes |
| **Favourite Questions** | No | Yes |
| **Smart Review** | No | Yes |

### Products

| Product ID | Duration | Price (CAD) |
|---|---|---|
| `truenorth_pass_1_month` | 30 days | $7.99 |
| `truenorth_pass_3_month` | 90 days | $14.99 |

**These are one-time, non-renewing passes, not subscriptions.** The marketing
copy promises no subscriptions, no auto-renewal and no surprise charges, and the
implementation has to match: purchases are **consumed or finished** after
entitlement is granted, never left permanently owned, or the store would refuse
to sell the same pass again after it expired.

On Apple they are created as **Non-Renewing Subscriptions**, which live under
**Subscriptions** in App Store Connect rather than under In-App Purchases.

---

## 6. Where data lives, and this is the part that matters

The single most important table in this document. **Storage is currently split
between the cloud and the device, and that split is the source of a known
defect** (section 10).

### In Firestore, document `users/{uid}`

| Field | Type | Purpose |
|---|---|---|
| `freeTestsUsedToday` | Int | Daily allowance counter |
| `freeTestDate` | String, `YYYY-MM-DD` | Date the counter applies to |
| `subscriptionExpiresAtMillis` | Long? | Pass expiry; null or past means no pass |
| `processedPurchaseTokens` | List of String | Replay guard, see section 7 |
| `testDateMillis` | Long? | The user's real test date, drives the countdown |
| `onboardingCompleted` | Boolean | |
| `privacyConsentGiven` | Boolean | |
| `favoriteQuestionIds` | List of Int | |
| `reviewedFlashcardIds` | List of Int | |
| `reportedQuestionIds` | List of Int | |

### In Firestore, collection `questionReports`

One document per report: `questionId`, `userId`, `reason`, `note`, `createdAt`
(server timestamp). Written in a **batch** together with the user's
`reportedQuestionIds` update, so the two land atomically.

### In Room, local to the device only

| Table | Columns | Holds |
|---|---|---|
| `test_attempts` | `id`, `timestampMillis`, `correctCount`, `totalQuestions` | One row per completed test |
| `category_stats` | `id`, `attemptId`, `category`, `correctCount`, `totalCount` | Per-category tallies for an attempt |
| `question_results` | `questionId` (PK), `wasCorrect`, `lastAttemptMillis` | **Most recent outcome only**, not full history |

`question_results` deliberately keeps only the latest result per question.
"Missed" means the last attempt was wrong; answering correctly later overwrites
the row and clears it from Smart Review.

### In platform preferences, local and not account-tied

`ThemePreferencesRepository` (theme mode), `PracticeTestPreferencesRepository`
(last-used test configuration), `FlashcardProgressRepository`, and
`ReviewPromptRepository` (the "have we already asked for a store review" flag,
deliberately per-install because an app-review prompt is a device concern).

---

## 7. Billing implementation

`BillingRepository` is an `expect class` with a store-agnostic surface:
`connect()`, `queryProductPrices()`, `launchPurchase()`,
`queryExistingPurchases()`, `consumePurchase()`, plus a
`SharedFlow<PurchaseUpdate>`.

The flow is deliberately a `SharedFlow` rather than a per-collector
`callbackFlow`, because Play's `BillingClient` accepts exactly **one**
`PurchasesUpdatedListener` at build time.

### Grant order, which is not negotiable

1. A purchase resolves
2. `UsageRepository.grantPass(token, duration)` writes entitlement **in a Firestore transaction**
3. **Only then** `consumePurchase()`

Consuming first and failing the grant afterwards would make the purchase
unrecoverable, because the store forgets consumed purchases.

### Why `grantPass` is a transaction

A purchase token can resurface through `queryExistingPurchases()` after a process
death between granting and consuming, and two purchases resolving close together
could race and lose one grant. `processedPurchaseTokens` makes replaying the same
token a no-op instead of double-stacking time.

### Stacking

Buying while time remains extends from the **existing expiry**, not from now:
`max(now, currentExpiry) + duration`. A sanity clamp caps any expiry at
**400 days** out, so a future stacking bug cannot compound silently.

### Pricing display

Prices come from the store at runtime, never hardcoded, so they are already
correctly localised. The per-month line on the 3-month pass is computed from
real price micros divided by 3, **rounded not truncated**. An earlier truncation
bug displayed $14.99/3 as "$4.99" instead of "$5.00".

---

## 8. Authentication

Email and password via Firebase Auth. **Sign-in is mandatory**; `App.kt` shows
`AuthScreen` whenever the current user is null, and `AppNavHost` otherwise.

`AuthRepository` covers sign-up, sign-in, password reset, email verification,
reload, sign-out, account deletion and re-authentication.

Two behaviours worth knowing:

- **Email-verified status is cached client-side.** There is no push when the
  user clicks the link in their inbox, so anything displaying verification
  status must call `reloadCurrentUser()` first.
- **Account deletion requires a recent sign-in.** If the session is stale,
  Firebase throws a re-authentication-required error; the caller must prompt for
  the password via `reauthenticate()` and retry.

---

## 9. Design system

| Token | Value | Use |
|---|---|---|
| `CanadaRed` | `#D80621` | Primary brand colour, in-app |
| `CanadaRedLight` | `#FF6B78` | Dark-theme primary |
| `CanadaRedContainerLight` | `#FFDAD9` | |
| `CanadaRedContainerDark` | `#6E0016` | |
| `Charcoal` | `#1C1B1F` | |
| `OffWhite` | `#FFFBFE` | |
| `DarkBackground` | `#121212` | |
| `DarkSurface` | `#1C1B1F` | |
| `LightOnDark` | `#E6E1E5` | |

**The app icon uses a different red: `#B6061A`**, noticeably deeper than
`CanadaRed`. Anything sitting flush against the icon should use `#B6061A`.

The icon itself is a white outlined maple leaf with a four-point north star at
its centre, on that red field. Master at `icon-source/TrueNorth Logo 2026.png`.

Theme modes: `LIGHT`, `DARK`, `SYSTEM`, stored per device, not per account.

Store marketing frames use a vertical gradient from `#E0332A` to `#A71519`,
sampled from the original Play screenshots. See
`tools/make-store-screenshots.ps1`.

---

## 10. Known limitations and accepted risks

**Record these rather than rediscovering them.**

### Test history does not sync across devices (open defect)

`test_attempts`, `category_stats` and `question_results` are Room-local. A user
who changes phone keeps their pass, favourites, flashcard progress and test date,
but loses **test history, the progress chart, the category breakdown, and Smart
Review**. Smart Review is a **paid** feature, so a paying customer sees it empty
on a new device with no explanation. Fix planned in
`docs/plan-cloud-sync-test-history.md`.

### Entitlement is client-trusted

There is no backend. Grants are written client-side straight to Firestore. A
technically motivated user editing Firestore could award themselves a pass. This
is the same trust model already used for the daily free-test counter, and was
accepted deliberately given the no-backend constraint. Server-side receipt
validation would need Cloud Functions plus the Play Developer API and the App
Store Server API.

### Auth errors are swallowed

The auth screen reports "Something went wrong. Please try again." for every
failure. This is why a misconfigured Firebase API key restriction broke all new
sign-ups for seven weeks in 2026 without anyone noticing.

### iOS unit tests cannot run

`:shared:iosSimulatorArm64Test` fails at link time with
`ld: framework 'FirebaseCore' not found`. The native Firebase frameworks come
from Swift Package Manager inside the Xcode project, so Gradle cannot see them
when linking a standalone Kotlin/Native test binary. The same tests pass against
the Android target, and the logic under test is pure common code.

### iOS review prompt is a no-op

`requestReviewFlow()` does nothing on iOS, because StoreKit's review request
needs a UIWindowScene that shared code cannot reach.

---

## 11. Platform-specific gotchas

- **The Xcode project uses `PBXFileSystemSynchronizedRootGroup`.** Any file
  dropped into `iosApp/iosApp/` is automatically in the target with no
  `project.pbxproj` entry. Never use File then Add Files there; just put the file
  in place. Doing otherwise creates duplicates like `PrivacyInfo 2.xcprivacy`.
- **Release signing on iOS is manual**, using the `TrueNorth App Store`
  provisioning profile and an Apple Distribution certificate. Automatic signing
  cannot work because the team has no registered devices.
- **`PrivacyInfo.xcprivacy` declares `NSUserDefaults` under reason `CA92.1`.**
  Required, because five iOS files use `NSUserDefaults`, which Apple classes as a
  required-reason API.
- **Android has two separate icons.** The 512x512 in Play Console is listing art
  only; the home-screen icon comes from the manifest's `android:icon`. Apple has
  no such split, which is why an iOS icon change needs a full rebuild.
- **Xcode build phases never source a shell profile.** The Run Script phase
  resolves `JAVA_HOME` itself, because an exported one only exists in Terminal.

---

## 12. Maintaining this document

Update it in the same commit as any change to: the question bank's size or
structure, where a piece of data is stored, the free/paid split, pricing or
product IDs, the scoring or selection rules, or anything in section 10.

A specification that drifts from the code is worse than none, because it gets
trusted.
