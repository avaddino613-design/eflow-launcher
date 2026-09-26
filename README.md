# PageFlow (working title)

A minimal Android launcher built around reading, matching the visual
language of the original PageFlow: E-Reader Launcher (light/dark cards,
colorful icon badges, a floating pill bottom nav, a persistent XP widget)
while being restyled enough to be clearly its own take rather than a
verbatim copy. Feature-wise it follows the original closely too, with two
kinds of differences:

1. **The one requested difference**: the app drawer recognizes system apps
   too, not just the ones with a normal launcher icon.
2. **A few small, low-risk additions** the redesign made natural to
   include: a real, functional Dark Mode toggle, a "Configure Apps"
   quick-launch row of up to 5 favorite apps on the Home screen, and a
   Settings hub tying it together.

EPUB support, app-blocking/time-limit features, Room, and kapt/KSP remain
removed from earlier drafts of this project - see "Why there's no
database" below for why that stays true even through this redesign.

## Screens

- **Home** - live clock, a "Continue reading" card when a book is in
  progress, and a row of up to 5 favorite apps you configure yourself.
- **Library** - search, All/Reading/Completed filter chips, PDF import via
  the system file picker.
- **Reader** - native `PdfRenderer`-based PDF viewer, remembers your last
  page. Deliberately has no bottom nav - full-screen reading, same as the
  original.
- **Apps** - the full app drawer, with the "Show system apps" switch
  described above.
- **Settings** - Dark Mode toggle (persisted, applied via
  `AppCompatDelegate` at app startup - follows the system setting until
  you explicitly choose), a link into Analytics (the Stats screen), and
  the default-launcher status/action.
- **Onboarding** - three steps (welcome, set as default launcher, done)
  with colored icon badges and a progress bar. "Next" is always tappable
  regardless of whether the launcher role has been granted yet, so
  there's nothing to get stuck on.
- Every main screen (Home/Library/Apps/Settings) shares the same floating
  bottom nav bar and the persistent "Level X Reader" XP widget docked just
  above it, tapping which opens the Stats detail screen.

## Design system

All colors are defined once as semantic names in `res/values/colors.xml`
(light) and mirrored in `res/values-night/colors.xml` (dark) - e.g.
`bg_card`, `text_primary`, `badge_blue_fg`. Using the same names in both
files means the OS's day/night switch (or the in-app Dark Mode toggle)
re-colors every screen automatically; nothing in the layouts or Kotlin
code needs to know which mode is active. Icons are small hand-drawn
vector XML files (`res/drawable/ic_*.xml`) - simple geometric shapes
(house, open book, grid, gear, trophy, arrows), not pulled from any icon
library, so there's no extra dependency.

## The one intentional feature difference: full app drawer

Normally, Android 11+'s package-visibility rules hide most other apps from
each other, and even without that, a typical launcher only lists apps that
expose a launcher icon (excluding a lot of system apps). Two things fix
that here (`apps/AppRepository.kt`):

1. Apps that hold the **HOME role** (i.e. are set as the default launcher)
   are automatically exempted by the OS from Android's package-visibility
   filtering. Once PageFlow is your default launcher, `PackageManager`
   calls already see every installed package - no `QUERY_ALL_PACKAGES`
   permission needed (which Play Store restricts heavily anyway).
2. A "Show system apps" switch in the app drawer additionally pulls in
   apps with no launcher icon at all, via `getInstalledApplications()`.
   Tapping one of those opens its system "App info" screen instead of
   silently failing, since it has no launch intent to open.

## Why there's no database

Persistence is a single JSON file in the app's private storage
(`data/LibraryStore.kt`), read/written with `org.json` - which ships in
the Android platform itself, so it's not even an extra dependency. This
app's data (a short book list and a log of reading sessions) is small
enough that a real database is more machinery than the job needs, and
skipping it also avoids something that has repeatedly broken the CI build
in earlier drafts: Room requires an annotation processor (KSP or kapt)
whose version has to stay in exact lockstep with the Kotlin compiler
version. Removing Room removes that entire category of failure.

Preferences (onboarding state, dark mode choice, favorite apps) are all
read/written through one small helper, `util/Prefs.kt`, rather than
scattered `getSharedPreferences()` calls.

## Toolchain versions

| Tool | Version |
|---|---|
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.1.20 |
| Gradle | 8.13 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 (Android 8.0+) |

No Room, no KSP, no kapt - just standard AndroidX libraries and Kotlin
coroutines, all of which are plain published artifacts with no
compiler-plugin version to keep in sync.

## Building an APK via GitHub Actions

Push this repo to GitHub (with the project's contents at the repo root)
and `.github/workflows/android-build.yml` builds a debug APK on every push
to `main`, and on manual runs from the Actions tab. The result is attached
to the run as a downloadable artifact named `pageflow-debug-apk`.

The workflow installs Gradle directly via `gradle/actions/setup-gradle`
rather than relying on a committed `gradlew` wrapper (this repo doesn't
include the wrapper's binary jar), and installs the specific Android SDK
platform/build-tools (API 36) this project targets on top of what
GitHub's runners already ship.

## Opening the project locally (Android Studio)

1. Install [Android Studio](https://developer.android.com/studio).
2. `File → Open` and select the project folder. Android Studio will offer
   to generate the missing Gradle wrapper - accept that, or run
   `gradle wrapper --gradle-version 8.13` yourself once if you have Gradle
   installed.
3. Let Gradle sync (needs network access this sandbox doesn't have, so
   this hasn't been compiled here - treat it as a strong scaffold, not a
   tested binary, until your first sync/build completes).
4. Run on a device or emulator running **Android 8.0 (API 26) or newer**.

## Permissions

Just the HOME role (requested via `RoleManager`, one tap in onboarding)
and the standard Storage Access Framework file picker for importing
books - no special/manual Settings-screen permissions needed at all.
