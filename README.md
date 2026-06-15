# Sevam Partner Android

Sevam Partner is the worker-facing Android app for Sevam service professionals such as plumbers, electricians, labourers, house helpers, home cooks, and grooming workers.

This first milestone is a native Kotlin/Compose app shell with mock-backed worker flows and repository interfaces ready for future backend integration.

## Current Scope

- Phone OTP login UI with debug-only continue path.
- Worker onboarding:
  - basic profile
  - KYC document submission
  - service category selection
  - skills, experience, tools, service area, availability, and work type
  - payout details
  - admin review gate
- Bottom navigation:
  - Home
  - Jobs
  - Earnings
  - Profile
- Mock job requests, active jobs, completed jobs, earnings, payout history, and support categories.
- Workers cannot go online until approval status is `Approved`.

## Build

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
```

## Local Config

Keep machine-specific values in `local.properties`. Do not commit API keys or SDK paths.

The v1 worker milestone does not require real backend keys because worker-specific data is mocked.

## Next Backend Work

Real integrations should replace the mock repository layer for:

- OTP/session restore
- partner profile
- KYC document upload and status
- category/skills catalog
- admin approval status
- job requests and job progress
- earnings and payout history
- support tickets
