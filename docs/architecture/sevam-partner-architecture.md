# Sevam Partner Architecture

The first Sevam Partner milestone is intentionally small:

- `app`: partner app shell, auth mock, onboarding, jobs, earnings, profile, and mock worker state.
- `core:ui`: shared Sevam Material theme and reusable UI primitives.

Worker-specific flows are mock-backed through `PartnerRepository` so real backend APIs can replace the implementation later without changing screen contracts.

## Backend Integration Targets

Future repository implementations should connect:

- phone OTP/session restore
- partner profile
- KYC upload and status
- category and skills catalog
- payout details
- admin approval status
- job requests and active job progress
- earnings and payout history
- support tickets
