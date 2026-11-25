# Build failure RCA and peer review — 2026-11-25

## Incident
GitHub Actions run [45675936339](https://github.com/aragheb77/Multiplayer_Connectivity_POC/actions/runs/45675936339) failed during the `Build both sample APKs` step.

## Evidence from the runner
- JDK 17 setup: passed.
- Gradle 8.13 setup: passed.
- Source payload extraction: passed as a command, but extracted content was incomplete.
- Gradle error: `Directory .../LocalMultiplayerPOC does not contain a Gradle build`.
- No `settings.gradle(.kts)` or root `build.gradle(.kts)` was present in the extracted build directory.
- APK verification and artifact upload were skipped; no APK artifact was produced.

## Root cause
The source payload archive committed to the repository was packaged from the wrong local directory. That directory contained only a README, rather than the reviewed Android project. This is a packaging/selection error, not a Gradle version or JDK setup failure.

## Corrective actions
1. Regenerate the CI payload from the reviewed source directory, whose root contains `settings.gradle.kts`, `build.gradle.kts`, `connectivity/`, `sample-app/`, and `sample-app-two/`.
2. Add a fail-fast source-layout check immediately after extraction so missing Gradle settings or sample modules are diagnosed before invoking Gradle.
3. Run `gradle --no-daemon assembleDebug` against the validated project root.
4. Require both sample APK outputs before artifact upload.
5. Do not mark build passed until the workflow succeeds and both APKs are downloadable.
6. After successful CI, perform the two-device Nearby Connections acceptance test; CI compilation alone is not a radio/device test.

## Peer-review disposition
**Rejected for release / APK delivery** until the correct source payload is committed and a successful build produces both APKs. The failure was preventable by validating the payload contents before committing and by validating the extracted project layout in CI.

## Current limitation
The corrected source archive has been regenerated in the working session, but this GitHub connector exposes text-file and Git-object operations only; it cannot transfer the local binary archive directly. The corrected archive must be uploaded to the repository using GitHub Desktop or another authenticated binary-capable Git client before CI can build it. No successful APK or device test is claimed.
