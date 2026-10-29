# App Review: responses for Guideline 2.1 Information Needed

Apple rejected build **1.0 (2)** on 2026-10-20 under **Guideline 2.1 - Information
Needed**. Despite the word "rejected", this is not a quality finding. The notice
opens with *"submitted by a developer account that has a limited App Review
history"*, and it is the standard questionnaire most first-time developers
receive. Both in-app purchases were marked **Ready for Review**, meaning Apple
accepted them and is holding them until the app version clears.

Keep this file updated. Apple asks for the same information on every submission
until it is recorded in the App Review Information **Notes** field.

---

## What Apple asked for

1. A screen recording on a **physical device**
2. Purpose and target audience
3. Setup and access instructions
4. External services used
5. Regional differences
6. Regulated industry or third-party material
7. In-app purchase overview

Items 2 to 7 are below, ready to paste. Item 1 has to be filmed.

---

## 1. The screen recording

**Must be a physical iPhone, not the Simulator.** Apple states this explicitly,
and a Simulator capture is rejected on its own terms.

Install the build through **TestFlight**, then record with Control Centre.

Capture in one take:

- Launching the app
- **Registering a new account**, performed live
- Signing in
- Home, starting a practice test, answering questions, seeing an explanation,
  finishing, viewing results and progress
- Reaching the **paywall** and the purchase flow
- **Deleting an account**, which Apple requires of any app with account creation

**Register a throwaway account on camera and delete that one.** Do not delete
`t.north.apps+appreview@gmail.com`, or the credentials given to Apple stop
working and the next submission fails for a new reason.

---

## A note on the demo password

**This repository is public, so the demo account password is deliberately not
written here.** It lives in App Store Connect under **App Review Information →
Sign-In Information**, which is where Apple reads it from anyway. Paste it into
the reply by hand, and never into a file in this repo.

The same applies to anything else that unlocks a real account.

---

## 2. Purpose and target audience

```
Citizenship Test Canada is an independent study app that helps adults prepare for the Canadian citizenship knowledge test.

Target audience: permanent residents of Canada, aged 18 to 54, who have applied for citizenship and are required to pass the knowledge test. Many are preparing while living outside Canada.

The problem it solves: the official government study guide, "Discover Canada," is roughly 70 pages of prose with no practice questions and no way to measure readiness. Applicants have one chance at the test and no reliable way to know whether they are prepared.

The value it provides: 589 practice questions across the six topics the real test covers, timed practice tests that mirror the official format (20 questions, 45 minutes, 75% pass mark), a written explanation for every answer citing the relevant page of the official guide, flash cards, and progress tracking by category so users can see exactly which topics still need work.
```

## 3. Setup and access instructions

```
Sign-in is required. Demo account:

Email: t.north.apps+appreview@gmail.com
Password: <see App Store Connect, App Review Information, Sign-In Information>

Steps after launching:
1. Sign in with the credentials above.
2. A privacy consent dialog appears. Tap to accept.
3. Onboarding asks for your citizenship test date. This can be skipped with "I don't have a date yet."
4. The Home screen shows your stats and a menu.

Main features and how to reach them, all from the Home screen:
- Practice Test: a timed 20-question test. Tap the settings icon on that row to change the length, filter by category, or filter by question type.
- Flash Cards: pick a category, then swipe through cards.
- Progress: scores over time and a per-category breakdown.
- How the Real Test Works: a guide to the official test day.
- Settings: theme, test date, account deletion, feedback.

There is only one account type. No sample files are required.
```

## 4. External services

```
- Firebase Authentication (Google): email and password account creation and sign-in.
- Cloud Firestore (Google): stores account preferences, the daily free-test counter, favourites, flashcard progress, and purchase entitlement.
- Apple In-App Purchase (StoreKit): all payment processing. The app never receives or stores payment details.

The app uses no AI services, no third-party data providers, no analytics SDKs, no advertising networks, and no third-party trackers. All question content is authored by the developer and bundled in the app; it is not fetched from any server.
```

## 5. Regional differences

```
The app functions identically in all regions. There are no regional differences in features, content, or behaviour.

The subject matter is Canada-specific, but the app is available worldwide because applicants frequently prepare while living abroad. In-app purchase prices are set with Canada as the base region and are converted automatically by the App Store for other storefronts.
```

## 6. Regulated industry and third-party material

```
This app does not operate in a regulated industry and provides no regulated service.

It is a study aid only. It does not provide legal advice, immigration advice, or immigration consulting, does not process or assist with any application, and creates no advisory or professional relationship. The Terms of Service state this explicitly, and the app repeats it in the "How the Real Test Works" screen and in Settings.

The app is not affiliated with, endorsed by, or connected to the Government of Canada or Immigration, Refugees and Citizenship Canada (IRCC). That disclaimer appears in the app, in the App Store description, in the privacy policy, and in the terms of service.

All 589 questions and their explanations are original wording written by the developer. They are based on facts from "Discover Canada," a publication of the Government of Canada released under the Open Government Licence - Canada and available free to the public. The app cites page numbers in that guide but reproduces none of its text. No third-party copyrighted material is included.

There is no user-generated content in the app, so content reporting and blocking requirements do not apply. The app does include a "Report an issue" button on each question, which sends a private message to the developer about a possible content error. It does not publish anything or make anything visible to other users.
```

## 7. In-app purchase overview

```
There are two in-app purchases. Both are one-time, non-renewing passes that simply expire. Neither auto-renews, and the app never charges a user again without a new, explicit purchase.

- 1-Month Pass (truenorth_pass_1_month), CAD $7.99, grants 30 days of access
- 3-Month Pass (truenorth_pass_3_month), CAD $14.99, grants 90 days of access

What a pass unlocks:
- Unlimited practice tests, instead of one free test per day
- Smart Review, which builds a test from only the questions you previously answered incorrectly
- Favourite Questions, which saves starred questions to a dedicated list

Everything else stays free forever: flash cards in all categories, progress tracking, the test date countdown, the test day guide, and one full practice test every day.

How to reach the purchase flow, two ways:
1. From Home, tap "Smart Review" or "Favourite Questions". Because these are paid features, the paywall opens immediately.
2. From Home, tap "Practice Test" after the day's free test has been used. The paywall opens instead of starting a test.

The paywall shows a free-versus-paid comparison table and both passes with live App Store pricing. Tapping either "Choose 1-Month Pass" or "Choose 3-Month Pass" opens the standard StoreKit purchase sheet.

Note for testing: the demo account's free test may already be used for the day, which means tapping Practice Test will take you straight to the paywall. That is expected behaviour, not an error.
```

## Note about pricing in the recording

Include this. It pre-empts a question rather than waiting to be asked.

```
One note on the recording: the paywall shows USD pricing ($5.99 and $9.99) while the purchase sheet shows the correct Canadian price ($7.99). This is a TestFlight sandbox artifact. Prices are read directly from StoreKit via SKProduct.price and SKProduct.priceLocale, with no hardcoded or converted values anywhere in the app, so each customer sees the price of their own storefront and is charged exactly that amount. The App Store Connect configuration sets Canada as the base region at CAD $7.99 and CAD $14.99, with all other storefronts derived automatically by Apple.
```

Background on that mismatch, and what was ruled out, is in
`project_truenorth_ios_price_mismatch` in the assistant's memory and summarised
in `docs/SPECIFICATION.md` section 10.

---

## Where to submit

1. App Store Connect → the app → the rejected submission → the **Messages**
   thread. Reply there with items 2 to 7, the pricing note, and the recording
   attached.
2. Paste the same text into **App Review Information → Notes** on the version
   page. Apple asks for this explicitly, and it saves repeating the exercise on
   every future submission.
3. **Resubmit to App Review** once the reply is in.

**No new build is required.** Build 1.0 (2) is already uploaded, processed and
attached to the version. This is a reply and a resubmission, not a release.

---

## Condensed version that fits the Reply box

**The Reply field and the App Review Notes field both cap at 4000 characters.**
The full answers above are about 6000, so they do not fit. The version below is
**3971 characters** and covers all seven items plus the pricing note. Paste the
same text into both places.

```
1. SCREEN RECORDING
Attached, recorded on a physical iPhone.

2. PURPOSE AND AUDIENCE
Citizenship Test Canada is an independent study app for adults preparing for the Canadian citizenship knowledge test. Audience: permanent residents aged 18 to 54 who have applied for citizenship, many preparing from outside Canada. The official guide, "Discover Canada", is roughly 70 pages of prose with no practice questions, so applicants have no way to gauge readiness before their single attempt. The app provides 589 practice questions across the six tested topics, timed tests mirroring the official format (20 questions, 45 minutes, 75% pass mark), a written explanation for every answer citing the guide's page, flash cards, and per-category progress tracking.

3. SETUP AND ACCESS
Sign-in is required. Demo credentials are in App Review Information.
After launch: sign in, accept the privacy dialog, optionally set a test date (skippable), then Home.
From Home: Practice Test (tap that row's settings icon to change length, category or question type); Flash Cards (pick a category); Progress; How the Real Test Works; Settings (theme, test date, account deletion, feedback).
One account type; no sample files.

4. EXTERNAL SERVICES
Firebase Authentication (Google) for email and password sign-in. Cloud Firestore (Google) for account preferences, the daily free-test counter, favourites, flashcard progress and purchase entitlement. Apple In-App Purchase (StoreKit) for all payments; the app never receives or stores payment details.
No AI services, no third-party data providers, no analytics SDKs, no ad networks and no trackers. All question content is authored by the developer and bundled in the app.

5. REGIONAL DIFFERENCES
None. Behaviour is identical everywhere. The subject matter is Canada-specific but sold worldwide because applicants often prepare abroad. Canada is the base price region; Apple derives all others.

6. REGULATED INDUSTRY AND THIRD-PARTY MATERIAL
Not a regulated industry and no regulated service. A study aid only: no legal, immigration or consulting advice, no application processing, no advisory relationship. Not affiliated with or endorsed by the Government of Canada or IRCC, a disclaimer shown in the app, the listing, the privacy policy and the terms.
All 589 questions and explanations are original wording by the developer, based on facts from "Discover Canada" (Government of Canada, Open Government Licence - Canada). Page numbers are cited; no text is reproduced. No third-party copyrighted material is included.
There is no user-generated content, so reporting and blocking requirements do not apply. A "Report an issue" button sends a private note to the developer about a possible content error; nothing is published or shown to other users.

7. IN-APP PURCHASES
Two one-time, non-renewing passes. Neither auto-renews.
1-Month Pass (truenorth_pass_1_month), CAD 7.99, grants 30 days.
3-Month Pass (truenorth_pass_3_month), CAD 14.99, grants 90 days.
A pass unlocks unlimited practice tests instead of one per day, Smart Review (a test built only from questions previously answered incorrectly), and Favourite Questions.
Free permanently: flash cards in all categories, progress tracking, the test-date countdown, the test-day guide, and one full practice test every day.
To reach it: from Home tap "Smart Review" or "Favourite Questions" (paid, so the paywall opens), or tap "Practice Test" once the day's free test is used. The paywall shows a free versus paid comparison and both passes at live App Store prices. Tapping either opens the StoreKit sheet.

PRICING NOTE
In the recording the paywall shows USD (5.99 and 9.99) while the purchase sheet shows the correct Canadian price (7.99). This is a TestFlight sandbox artifact. Prices are read directly from SKProduct.price and SKProduct.priceLocale with no hardcoded or converted values anywhere in the app, so every customer sees and is charged their own storefront's price.
```
