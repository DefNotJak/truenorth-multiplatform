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

### A revealed password uses a plain text keyboard
*2026-11-01*

While `passwordVisible` is true, the two `AuthScreen` password fields pass
`KeyboardType.Text` instead of `KeyboardType.Password`, with
`autoCorrectEnabled = false` and `capitalization = KeyboardCapitalization.None`.

This looks like a security regression and is not one. Compose's iOS text input
view derives `isSecureTextEntry` from the `KeyboardType`, and **iOS disables the
keyboard drag-to-move-cursor gesture in secure text entry fields**. That is
Apple's behaviour, not a framework bug; Flutter has the same report. So a
password field could never support the gesture the owner asked for.

The eye icon has already put the password on screen in plain text by the time
this applies, so secure text entry is protecting nothing at that point. What it
is still costing is the gesture. Rejected: leaving it as `Password` always,
which keeps a flag with no remaining benefit; and removing the reveal toggle,
which is worse for exactly the users who most need to check what they typed.

The autocorrect and capitalisation flags are not optional extras here. A plain
text keyboard would otherwise autocorrect and sentence-capitalise a password,
which would be a real bug.

### Keyboard insets are handled per screen, not globally
*2026-11-01*

`AuthScreen` carries `.imePadding()` on its scrolling column. The alternative
considered and rejected was setting `contentWindowInsets` on the `Scaffold`, or
applying it once somewhere central.

Rejected because the app has only three text fields, and the other two are
inside Material3 `AlertDialog`s, which get their own window and are centred in
the space the keyboard leaves. A global change would alter the inset behaviour
of every screen in the app, including ones that never take text input, to fix
one screen. The cost of the per-screen approach is that a future screen with a
text field can forget it, which is why this is recorded here and in
`docs/SPECIFICATION.md` section 8 rather than left to be rediscovered.

### Test history is mirrored to Firestore, not moved there
*2026-11-01*

The three Room tables stay the source of truth for every read. Firestore gets a
copy. Rejected: replacing Room with Firestore, which would have meant routing
the Home stats row and the Progress chart through listeners that cold-start
empty, and re-testing every screen that touches history. A mirror can be
reverted by deleting one class; a migration cannot.

Attempt documents are keyed by their own contents
(`"{timestampMillis}-{correctCount}-{totalQuestions}"`) rather than a Firestore
auto-id, so an upload that fails after writing but before recording success
overwrites one document instead of inventing a second attempt. Two attempts in
the same millisecond with identical scores collide and collapse into one; a
practice test runs for minutes, so this cannot realistically happen, and the
alternative duplicates on every retry.

### Local history is wiped when a different account signs in
*2026-11-01*

`sync_state.ownerUid` records which uid the Room database belongs to. If the
signed-in uid differs, local history is deleted before anything can upload it.

This destroys data, deliberately. The alternative is worse in kind rather than
degree: without the check, the first person's unsynced attempts are uploaded
into the second person's cloud account, permanently, and there is no mechanism
by which either of them could ever discover why their numbers are wrong. They
cannot be rescued before the wipe, because by then the session belongs to the
second person and the security rules correctly refuse to let them write to the
first person's document.

Scope of the loss: only attempts that never reached the cloud, and only when
someone signs out and a different account signs in on the same device. Before
this change those rows were not destroyed, but they were visible to the second
person, which was already a bug.

Known gap: a device that already had history when it upgraded to schema 2 has
`ownerUid` null, so whoever signs in first claims it. Closing that would need
ownership information the old schema never recorded.

### `syncMissedQuestionIds()` is gated on a successful restore
*2026-11-01*

The Smart Review set is written as a whole-array replace, not `arrayUnion`,
because answering a previously missed question correctly has to remove it; a
union would grow monotonically until Smart Review served questions the user had
already mastered.

That makes it destructive, which matters in one specific sequence: new device,
cold start, restore fails on a dead network, user backgrounds and resumes, and
the resume path pushes the empty local set over the real one. Smart Review is
then empty for that account on every device, permanently, and the user has paid
for it. A session flag set by a successful restore is what prevents this.

Uploading attempts is deliberately *not* gated the same way. It only ever adds
documents, so running it before a restore is harmless, and gating it would
block the one path that carries an existing user's local-only history up.

### IAP prices are re-queried on every resume
*2026-11-21*

The cold-start `LaunchedEffect(Unit)` was the only price fetch in the process.
Anything wrong with it stayed on screen until the app was killed: a fetch that
failed on a dead network, or one that resolved against the wrong storefront.
The storefront can also change legitimately, when someone switches their App
Store region, and that happens while the app is backgrounded.

The resume path already reconnected and reconciled purchases, so the price
query joins it there.

The non-empty guard is the load-bearing part. `queryProductPrices()` returns an
empty map on failure, so an unguarded assignment would let one bad resume
replace correct prices with a paywall showing none. Overwriting only on a
non-empty result makes a failed refresh a no-op instead.

This is not confirmed to be the cause of the USD/CAD paywall mismatch, which
remains open. It is worth doing either way, and it means a transient bad fetch
self-corrects rather than persisting.

### Sign in with Google is Android-only, on purpose
*2026-11-03*

`GoogleSignIn` has an Android actual built on Credential Manager and an iOS
actual that reports `isAvailable = false`. `AuthScreen` omits the button
entirely when that flag is false, rather than showing one that throws.

The constraint is App Store review guideline 4.8: an iOS app offering a
third-party sign-in service must also offer Sign in with Apple. Shipping Google
to iOS alone would fail review, so the two belong in one piece of work. Until
then a stub that fails on tap would be worse than no button, because the user
discovers the gap after committing to it.

Credential Manager rather than `GoogleSignInClient`, which Google deprecated.
Beyond that, the same request can later carry passkey and saved-password
options, so those become a credential option rather than a second integration.

The web client ID is read with `resources.getIdentifier`, not through `R`. The
google-services plugin writes `default_web_client_id` into the application
module; `shared` is a library module and its `R` holds only its own resources.
The resource is present in the merged APK at runtime, which is all the lookup
needs, and a missing one throws a message naming the console step instead of a
bare resource ID.

`googleid` is pinned to 1.2.1 here and 1.1.1 in the native Android repo. 1.2.x
ships Kotlin 2.4 metadata; this project compiles with Kotlin 2.4.10 and reads
it, the other is on 2.2 and does not.

### firebase-ios-sdk pinned to 13.1.1 for the eMudhra CA revocation
*2026-11-03*

On 2026-10-17 Google disclosed that private keys for several intermediate CA
certificates issued by eMudhra were compromised. The affected Firebase SDKs
compile the TLS root certificate bundle into the application binary, so neither
an OS update nor a server-side change fixes an already-installed build. A
network attacker holding those keys could forge a Firestore certificate that an
unpatched client would still trust, and read or tamper with everything in
transit.

firebase-ios-sdk 13.1.1 removes the affected roots from the embedded trust
store. The Xcode SPM reference now requires it as a minimum rather than 13.0.1.

Only the iOS app was exposed. Google's notice lists the Apple, Flutter, C++ and
Unity SDKs plus community-supported wrappers as affected, and explicitly
excludes the Firebase Android and Web SDKs and the backend service. The native
Android app and this project's androidApp both go through the Android SDK, so
neither needed a rebuild.

### The iOS product request waits for the storefront
*2026-11-21*

`queryProductPrices()` now polls `SKPaymentQueue.storefront` until it is
non-null, up to three seconds, before issuing the `SKProductsRequest`.

`storefront` is null for a short window after the payment queue comes up, and a
products request issued inside that window returns prices for a default
storefront instead of the user's. That matches the USD-on-a-Canadian-account
bug exactly: the paywall queries at cold start and showed $5.99/$9.99, which
are base-storefront values, while the payment sheet seconds later charged
$7.99, the correct Canadian price. Same `SKProduct` object, two different
answers, because only one of them was resolved after the storefront was known.

Ruled out first, on a real device: a stale cached product response (clean
delete and reinstall reproduced it), our own display code (no hardcoded prices
or fallbacks anywhere in `buildPlans`), and a signed-in sandbox tester with a
US region (none is signed in at all, which is itself consistent with a default
storefront being used).

Not yet confirmed as the fix. Every observation so far is from TestFlight,
which uses the StoreKit sandbox, so the bug may not exist in production. The
timeout returns null rather than blocking, because a paywall with uncertain
prices beats one that never loads, and the resume refresh gets another attempt
a moment later.

### Sign in with Apple is pure Kotlin/Native, no Swift bridge
*2026-11-21*

`AuthenticationServices` is an Objective-C framework, so `ASAuthorizationAppleIDProvider`,
`ASAuthorizationController` and both of its delegate protocols are reachable
from `iosMain` through cinterop. SHA-256 comes from `platform.CoreCrypto`. No
Swift file, no extra Swift Package, nothing added to the Xcode project but the
capability.

Three things in that implementation are load-bearing and should not be
simplified:

- **The delegate is a field, not a local.** `ASAuthorizationController` holds
  its delegate and presentation provider weakly. A local is collected while
  the sheet is still open and no callback ever arrives.
- **The raw nonce travels with the token.** Apple receives the SHA-256 and
  embeds it in the identity token; Firebase receives the raw value and checks
  the two agree. `AppleCredential` keeps them together so they cannot be
  swapped, which otherwise fails with an unhelpful error.
- **The nonce comes from two `NSUUID`s, not `kotlin.random.Random`.** Random is
  not a cryptographic source, and the whole point of the nonce is that it
  cannot be guessed.

Google on iOS is deliberately not part of this. GitLive's wrapper exposes
`signInWithCredential` but no `signInWithProvider`, confirmed by reading the
2.6.0 metadata, so Firebase's own web OAuth flow is not reachable and a Google
ID token on iOS requires the GoogleSignIn SDK behind a Swift bridge. That is a
separate piece of work, and it matters because Android users who signed up
with Google after 2026-11-05 have passwordless accounts they cannot currently
reach from an iPhone.

### Google sign-in on iOS goes through a Swift bridge, Apple does not
*2026-11-21*

The two look symmetrical from the shared screen and are not symmetrical
underneath. `AuthenticationServices` is Objective-C, so Apple sign-in is
written entirely in `iosMain`. Google's iOS SDK has a Swift-only surface that
cinterop cannot see, so that one keeps its native call in Swift and sends back
only the ID token.

Two alternatives were rejected. Firebase's own web OAuth flow is unreachable
because GitLive exposes `signInWithCredential` but no `signInWithProvider`,
confirmed by reading the 2.6.0 metadata. Hand-rolling OAuth with PKCE over
`ASWebAuthenticationSession` would stay in Kotlin, but it means an HTTP client
this project does not otherwise carry, plus roughly two hundred lines of
security-sensitive code in exchange for avoiding forty lines of Swift against
a documented API.

The Swift lives in `iOSApp.swift` and `ContentView.swift`, both already in the
Xcode target, rather than a new file. A new Swift file has to be registered in
`project.pbxproj`, which means a step in Xcode; editing files that are already
there can be done from any machine. The only Xcode action this feature needs
is adding the GoogleSignIn-iOS package.

`GoogleSignInHandler` passes three single-argument callbacks instead of one
multi-argument callback. Kotlin lambdas carrying a nullable String next to a
Boolean arrive in Swift with boxed types like `KotlinBoolean`, and nothing
checks this boundary end to end, so the simplest possible signatures are worth
more than the tidier shape.
