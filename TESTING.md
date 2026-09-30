# Testing FocusFlame

## Free Timer

1. Open the app with no `REVENUECAT_PUBLIC_SDK_KEY` in `local.properties`.
2. Confirm the 25-minute duration is selectable and Pro durations are disabled.
3. Tap **Light candle**.
4. Confirm the countdown changes based on elapsed time and the candle gets shorter.
5. Tap **End early** and confirm the session clears.

## Distraction Tracking

1. Start a session.
2. Press Home while the screen is still on.
3. Wait a few seconds.
4. Return to FocusFlame.
5. Confirm a wax drip appears near the candle height where you left.
6. Confirm distraction count increases and total time away is shown.

## Screen-Off Case

1. Start a session.
2. Turn the screen off with the power button.
3. Turn it back on and unlock.
4. Return to FocusFlame.
5. Confirm no distraction is added for screen-off backgrounding.

## Session Completion

1. For quick manual testing, temporarily choose a shorter Pro/custom duration after enabling Pro, or reduce the duration in code.
2. Let the session finish.
3. Confirm the result panel shows focus duration, distraction count, and total time away.
4. Restart the app and confirm the last candle persists.

## RevenueCat Test Store

1. Copy `local.properties.example` to `local.properties`.
2. Set `REVENUECAT_PUBLIC_SDK_KEY` to your RevenueCat Test Store public key.
3. Confirm the app shows a RevenueCat status instead of the missing-key message.
4. Configure a `pro` entitlement and current offering in RevenueCat.
5. Tap **Upgrade** and complete a Test Store purchase.
6. Confirm Candle Shelf and custom durations unlock.
7. Use **Restore** to verify entitlement refresh.

If RevenueCat fails to configure or offerings are unavailable, verify the Test Store key, package name, entitlement id `pro`, and current offering setup. The free timer should remain usable either way.
