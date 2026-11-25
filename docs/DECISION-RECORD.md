# Decision Record — Multiplayer Connectivity POC

**Decision:** REJECTED / STOPPED  
**Date:** 2026-11-25  
**Disposition:** Restart from scratch later as a new POC.  
**Authority:** Project owner

## Decision

This POC is rejected. Do not integrate, ship, or treat the current implementation as an approved reusable component. Retain the repository only as historical reference until a fresh POC is initiated.

## Basis

- The Android CI workflow repeatedly failed and never verified successful builds of both sample applications.
- The initial workflow failure was caused by unavailable legacy Android SDK package installation.
- After correcting SDK setup, compilation exposed a Kotlin metadata/toolchain mismatch and unresolved task-listener APIs.
- A subsequent Kotlin compiler upgrade also failed during Gradle script compilation.
- No successful APK artifacts were confirmed.
- No two-device physical connectivity, messaging, lifecycle, or acceptance testing was completed.

Latest failed workflow: https://github.com/aragheb77/Multiplayer_Connectivity_POC/actions/runs/45681290264

## Restart guidance

When the owner chooses to restart, create a new POC rather than continuing this implementation. Re-establish the transport feasibility and supported dependency/toolchain versions first, then define explicit build, artifact, physical-device, and acceptance gates before implementation proceeds.

## Current status

No further implementation work is authorized under this rejected POC unless the owner explicitly reopens it.
