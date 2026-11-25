# Test report — initial source delivery

**Source review update:** 2026-11-25

**Physical-device test date:** Not performed

## Environment

| Item | Result |
|---|---|
| Authoring environment Java | Java executable detected; version not captured |
| Gradle | Not installed in authoring environment |
| Android SDK | Not installed/detected in authoring environment |
| Physical Android devices | Not available to this execution environment |
| Google Play services device versions | Not available |

## Status key

- PASS — executed and verified.
- FAIL — executed and did not meet expectation.
- INCOMPLETE — implementation/source exists but verification is incomplete.
- BLOCKED — cannot be executed in this environment.
- NOT STARTED — not attempted.

## Acceptance matrix

| ID | Scenario | Status | Evidence / next action |
|---|---|---|---|
| T01 | No internet; local communication | BLOCKED | Requires two physical devices; execute offline test with radios enabled |
| T02 | Mutual discovery | BLOCKED | Requires physical devices and build/install |
| T03 | Stop discovery | INCOMPLETE | Stop API implemented; runtime/resource behavior not verified |
| T04 | Initiate connection | BLOCKED | Requires physical devices |
| T05 | Accept connection | BLOCKED | Requires physical devices |
| T06 | Reject connection | BLOCKED | Requires physical devices |
| T07 | Authentication | BLOCKED | Manual code-comparison flow implemented; verify on two devices |
| T08 | A → B message | BLOCKED | Requires physical devices |
| T09 | B → A message | BLOCKED | Requires physical devices |
| T10 | Multiple messages | BLOCKED | Requires physical devices |
| T11 | Normal disconnect | BLOCKED | Requires physical devices |
| T12 | Unexpected disconnect | BLOCKED | Requires physical devices |
| T13 | Unavailable peer | INCOMPLETE | Failure handling implemented; runtime behavior not verified |
| T14 | Denied permission | INCOMPLETE | Runtime requests included; denial/revocation behavior needs device test |
| T15 | Disabled radio | INCOMPLETE | Primary sample displays radio state and manual settings guidance; OEM/API behavior not verified |
| T16 | Repeated cycles | BLOCKED | Requires physical devices and soak test |
| T17 | Authentication failure | BLOCKED | Requires physical devices; confirm no trusted state after rejection/failure |
| T18 | Independent consumer app | INCOMPLETE | Separate module/package uses the public API; sample 2 has duplicate-device handling and radio/permission gates in source, but build and runtime proof pending |

## Build status

**BLOCKED / NOT RUN.** No Gradle installation or Android SDK was available in the authoring environment. No APK is claimed or supplied. The GitHub Actions workflow is configured to build both debug APKs and upload them after a push, but that workflow has not been executed because a new repository could not be created from the available GitHub connector actions. Open the project in Android Studio and build `sample-app` and `sample-app-two`; fix any compile issues before installing.

## Device test record template

For each run, record: test ID, date/time, device model, Android version, Google Play services version, preconditions, steps, expected result, observed result, PASS/FAIL, logs, and issue reference.

No physical-device test is marked PASS in this report.
