# FocusFlame — Drip Record

“The candle remembers every time you left.”

FocusFlame is a tiny Android/Kotlin app built with Jetpack Compose and Material 3. It runs a focus timer represented by a virtual candle that burns down in real time. If the user backgrounds the app while the screen is still on, FocusFlame records a distraction and adds a wax drip at the candle height where it happened. Screen-off backgrounding is ignored.

## Project

- Package: `com.alexandrupop.focusflame`
- minSdk: 29
- UI: Jetpack Compose Canvas + Material 3
- Timing: timestamp-based with `SystemClock.elapsedRealtime()`
- Persistence: local `SharedPreferences`
- Backend/login/Firebase/notifications/widgets: none

## RevenueCat Test Store

The app integrates RevenueCat Test Store for the `focusflame_pro` entitlement. RevenueCat is optional at runtime: if the SDK key is blank, invalid, or unavailable, the free 25-minute timer still works.

1. Copy `local.properties.example` to `local.properties`.
2. Add your RevenueCat Test Store public SDK key:

```properties
REVENUECAT_PUBLIC_SDK_KEY=your_test_store_public_key
```

3. In RevenueCat, create an entitlement named `focusflame_pro`.
4. Add at least one package to the current offering for Test Store purchase testing.

Free users get 25-minute sessions and the last candle. Pro users get Candle Shelf history and custom durations.

## Open In Android Studio

Open the `FocusFlame` folder in Android Studio and let Gradle sync. Use a JDK 17 runtime and install Android SDK Platform 37 if prompted.

## Notes

The project intentionally stays small: one Activity, one ViewModel, local storage, a RevenueCat wrapper, and Compose UI. No backend, login, AI, Firebase, notifications, widgets, or extra features are included.
