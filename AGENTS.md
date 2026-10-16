# AmpyStargate continuity

Read `docs/AmpyStargate-Specifiche.md` and `README.md` before making changes. They preserve the user's intent across conversations. This repository is separate from AmpybiOS and Family Money Tracker: do not modify those projects as a side effect.

Keep components modular, prefix characteristic names with Ampy, and keep system graphics in editable SVG/vector form. Android app icons are owned by their apps. Target Pixel 7 Pro / Android 17; do not claim measured 120 fps or physical-device tests without evidence.

Global effects are explicitly opt-in and decorative. Respect inaccessible/sensitive UI. Do not add text, password or keystroke logging, screenshot recording, touch injection, fake accessibility-tool classification, or permission bypasses. Each new capability needs an explicit user-facing description and ordinary Android authorization where required.

Keep recovery in a separate process. Verify PID ownership before killing only our stalled process. Never auto-rearm overlays after service restarts or recovery. Do not promise recovery from global Android/GPU freezes or automatic replacement of the user's default launcher.

Run geometry/watchdog math tests, compile and lint. For lifecycle, touch pass-through and freeze recovery, use only the disposable CI emulator: provisioning scripts must never silently target a user's phone. Record which checks actually passed; registration of an IME does not prove every keyboard function or compatibility with external keyboards.
