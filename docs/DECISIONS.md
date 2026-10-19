# Decision log

Why things are the way they are. Several of these look wrong until you know the
constraint behind them, which is the entire reason this file exists.

**Append a new entry whenever a choice is made that a reasonable person might
later reverse without knowing why it was made.** Newest at the bottom. Keep each
one short: what was decided, why, and what was rejected.

Entries before 2026-10-18 were reconstructed from the code, commit history and
project notes, so their dates are approximate.

---

## Product

### One-time passes, not subscriptions
*Approx. 2026-08*

Passes are prepaid and expire. They do not auto-renew.

Partly a positioning choice, since "no subscriptions, no auto-renewal, no
surprise charges" is a real differentiator in a category full of traps. Partly
practical: the audience needs the app for a few weeks around one exam, not
indefinitely.

**Consequence that cannot be forgotten:** purchases must be consumed or finished
after entitlement is granted. Leaving them permanently owned means the store
refuses to sell the same pass again once it expires.

### One free practice test per day
*Approx. 2026-08*

Enough to prove the app works and build a habit; not enough to prepare for the
test. The counter is stored with the date it applies to, so any stored date
other than today reads as zero used. The allowance resets at local midnight with
no scheduled job.

### Favourites and Smart Review are the paid features
*Approx. 2026-09*

Everything that helps someone decide the app is good (flash cards, progress,
the test-day guide) stays free. The two features that only matter once you are
seriously studying sit behind the pass.

### Sign-in is mandatory
*Approx. 2026-07*

Rejected: anonymous local-only use. Entitlement has to survive a reinstall, and
without an account there is nothing to attach a pass to.

**Known cost:** it is a barrier on first launch, and it meant that when the
Firebase API key restriction broke authentication, the entire app was unusable
for new users rather than merely degraded.

---

## Architecture

### No backend
*Approx. 2026-07*

Entitlement grants are written client-side straight to Firestore. There is no
server, no Cloud Functions, and no receipt validation.

**Accepted risk:** someone editing their own Firestore document could award
themselves a pass. This is the same trust model already used for the daily
free-test counter. Server-side validation would need Cloud Functions plus the
Play Developer API and the App Store Server API, which is disproportionate at
this scale.

Revisit if revenue ever makes the fraud worth someone's time.

### Kotlin Multiplatform rather than a second native app
*Approx. 2026-08*

One shared UI and one shared business layer. The alternative was writing the iOS
app separately in Swift, which doubles every future change.

**Cost paid:** several weeks of porting, and a class of build problems that do
not exist in single-platform projects.

### StoreKit 1 on iOS, not StoreKit 2
*2026-09*

**StoreKit 2 is Swift-only and unreachable from Kotlin/Native cinterop.** The
only alternative was writing a Swift bridge module, which would have meant
maintaining platform-specific billing code in a project whose entire point is
shared code.

Not a preference. A constraint.

### Room stays the read path; Firestore is a mirror
*2026-10-18, planned, not implemented*

For the test-history sync work, Room remains the local source of truth for reads
and Firestore receives a copy.

Rejected: migrating reads to Firestore. The Progress screen and Home stats read
on every composition, and routing those through Firestore listeners risks
visible flicker on cold start. A mirror can also be reverted cleanly; a
migration cannot.

See `docs/plan-cloud-sync-test-history.md`.

---

## Content

### `topicGroupId` groups variants of the same fact
*Approx. 2026-09*

Questions testing the same underlying fact in different wordings share a group,
and test selection takes at most one per group.

Without it, a 20-question test could ask the same fact three times in different
phrasings, which feels broken and wastes the user's limited free test.

**Only give two questions the same group when they are genuinely
interchangeable.**

### `flashcardText` exists because flash cards show only the answer
*Approx. 2026-09*

A question phrased "Which of the following is..." reads fine in a practice test
with four visible options, and becomes meaningless on a card showing only the
correct one. 42 questions carry a standalone rewording used only by flash cards.

Rejected: rewording the questions themselves, which would have made them worse
in the practice test where they are mostly used.

### Flash cards exclude True/False questions answered "False"
*Approx. 2026-09*

A card reading "ANSWER: False" teaches nothing, and worse, risks the statement
being misremembered as true. Those questions remain fine in a practice test
where all options are shown.

### Questions are original wording, not reproduced from Discover Canada
*Approx. 2026-07*

Content is written from, and cites page numbers in, the official guide, but
reproduces none of its text. Facts are not copyrightable; the phrasing is.

This is what makes "No" the correct answer to App Store Connect's content rights
question.

---

## Store and platform

### iPhone only for the first iOS release
*2026-10-17*

iPad support was removed, and Mac and Apple Vision availability unticked, before
the first submission.

The layout has never been seen on any of those screens. Each one that is enabled
is a surface App Review can test and find wanting, in exchange for users who are
very unlikely to study for a citizenship test on a desktop or a headset.

**All three are reversible at any time without a new build.**

### Non-Renewing Subscription, not Consumable, on Apple
*2026-10-17*

Apple's own definition of a non-renewing subscription is access to content for a
limited duration that does not auto-renew, which describes the passes exactly. A
consumable is something depleted by use; a pass expires by date.

Functionally identical under StoreKit 1, so the choice cost nothing. **Product
type is permanent after creation.**

### Manual signing for the iOS Release configuration
*2026-10-17*

Automatic signing demands an *iOS App Development* provisioning profile, and
Apple will not issue one to a team with zero registered devices. Registering one
needs a physical iPhone cabled to the build machine, which is impossible on a
rented cloud Mac.

Distribution certificates and App Store profiles need no devices. So Release uses
manual signing with the `TrueNorth App Store` profile, while Debug stays
automatic.

### Business structure: sole proprietor, Apple Individual account
*2026-09*

Apple does not accept sole proprietorships for Organization enrolment; it
requires a separate legal entity that can contract in its own name.

**Consequence:** the owner's personal legal name appears as the seller on the
App Store, and no business name can replace it without incorporating.

### GST/HST registered voluntarily while under the $30k threshold
*2026-09-11*

Apple's Paid Applications Agreement requires both a Business Number and an RT
number via Form GST506, so registration was not optional in practice.

**Cost:** a permanent annual filing obligation that was not otherwise required.
Flagged for an accountant to review, due spring 2027.

---

## Security

### Firebase client config files are committed deliberately
*2026-09-01*

`google-services.json` and `GoogleService-Info.plist` are in the repository.
They contain client configuration that ships inside every copy of the app and
are not secrets. GitHub's secret scanner flags them anyway.

The Android API key was restricted in Google Cloud Console as defence in depth.

**That restriction was then misconfigured and silently broke every new sign-up,
sign-in and password reset for seven weeks**, because the SHA-1 entered matched
neither the app signing key nor the upload key. Existing users were unaffected,
since their session tokens persist, which is why nobody noticed.

**Lesson, worth more than the decision:** after restricting an API key, test the
thing it protects. The failure mode is silent and only affects new users.

### Commits use the business identity
*2026-10-18*

The three TrueNorth repositories commit as `TrueNorth Apps
<t.north.apps@gmail.com>` rather than the owner's personal address, which was
public in 122 existing commits across public repositories.

Existing history was left alone: rewriting it means force-pushing over published
history across three repos, and the exposure is a Gmail address in commit
metadata on a small project.
