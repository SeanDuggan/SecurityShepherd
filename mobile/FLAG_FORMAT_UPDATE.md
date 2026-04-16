# Mobile Shepherd Flag Format Update

## Overview
All flags in Mobile Security Shepherd have been standardized to use the **KEY{}** format. This document provides a comprehensive reference for all 26 module flags.

## Format Specification
- **Prefix**: `KEY{`
- **Content**: L33tspeak pattern (mix of numbers and letters)
- **Suffix**: `}`
- **Example**: `KEY{R3v3rs3_Eng1n33r1ng_M4st3r_2024}`

## Flag Validation
Flags are validated using SHA-256 hash comparison in `FlagValidator.java`. The submitted flag is hashed and compared against the pre-computed hash stored in the `FLAG_HASHES` HashMap.

## All FLAGS (26 Modules)

### Lessons (11)

| Module | Flag |
|--------|------|
| RE_LESSON | `KEY{R3v3rs3_Eng1n33r1ng_M4st3r_2024}` |
| IDS_LESSON | `KEY{1ns3cur3_D4t4_St0r4g3_L34k}` |
| POOR_AUTH_LESSON | `KEY{T4co_Sn0r3s_0n_4_C0uch}` |
| INSECURE_AUTH_LESSON | `KEY{Pr1v1l3g3_Esc4l4t10n_Pwn3d}` |
| SUPPLY_CHAIN_LESSON | `KEY{Vuln3r4bl3_D3p3nd3ncy}` |
| INSECURE_COMM_LESSON | `KEY{Unsecur3_HTTP_Tr4ff1c}` |
| INSUFFICIENT_CRYPTO_LESSON | `KEY{W3ak_DES_Encrypt10n}` |
| SECURITY_MISCONFIG_LESSON | `KEY{D3bugg4bl3_Fl4g_F0und}` |
| INPUT_VALIDATION_LESSON | `KEY{1nput_V4l1d4t10n_Byp4ss3d}` |
| PRIVACY_LESSON | `KEY{3x1f_M3t4d4t4_L34k5_L0c4t10n}` |
| CLIENT_SIDE_INJECTION_LESSON | `KEY{CL13NT_S1D3_SQL_1NJ3CT10N}` |

### Challenges (15)

| Module | Flag |
|--------|------|
| RE_CHALLENGE_1 | `KEY{S1mpl3_Fl4g_34sy_T0_F1nd}` |
| RE_CHALLENGE_2 | `KEY{B4s3_S1xty_F0ur_D3c0d3d}` |
| RE_CHALLENGE_3 | `KEY{0bfusc4t3d_Str1ng_F0und}` |
| IDS_CHALLENGE_1 | `KEY{SQLit3_D4t4_3xtr4ct3d}` |
| IDS_CHALLENGE_2 | `KEY{Sh4r3d_Pr3fs_L34k3d}` |
| POOR_AUTH_CHALLENGE | `KEY{P00r_Auth_W34k_Qu3st10ns}` |
| SUPPLY_CHAIN_CHALLENGE | `KEY{Vuln3r4bl3_D3p3nd3ncy}` |
| INSECURE_COMM_CHALLENGE | `KEY{N3tw0rk_Sn1ff3d}` |
| INSUFFICIENT_CRYPTO_CHALLENGE | `KEY{ECB_M0d3_Vuln3r4bl3}` |
| SECURITY_MISCONFIG_CHALLENGE_2 | `KEY{B4ckup_D4t4_3xtr4ct3d}` |
| SECURITY_MISCONFIG_CHALLENGE_3 | `KEY{3xp0rt3d_C0mp0n3nt_Pwn}` |
| XSS_CHALLENGE | `KEY{XSS_W3bV13w_Pwn3d_2024}` |
| CLIENT_SIDE_INJECTION_CHALLENGE_1 | `KEY{SQL_1nj3ct10n_4dm1n_Pwn}` |
| CLIENT_SIDE_INJECTION_CHALLENGE_2 | `KEY{UN10N_B4s3d_1nj3ct10n}` |

## Changes Made

### Files Modified (18 Java files)
1. **LessonFragment.java** - FLAG{} → KEY{}
2. **InputValidationLessonFragment.java** - FLAG{} → KEY{}
3. **XssWebViewChallengeFragment.java** - FLAG{} → KEY{}
4. **ClientSideInjectionLessonFragment.java** - FLAG{} → KEY{} (also updated contains check)
5. **InsecureAuthorizationLessonFragment.java** - Shepherd{} → KEY{}
6. **SupplyChainLessonFragment.java** - OWASP{} → KEY{}
7. **PrivacyControlsLessonFragment.java** - OWASP{} → KEY{}
8. **PoorAuthChallengeFragment.java** - OWASP{} → KEY{}
9. **ClientSideInjectionChallenge1Fragment.java** - OWASP{} → KEY{} (also updated comment)
10. **SecretActivity.java** - FLAG_PART1 = "OWASP{" → "KEY{"
11. **SecurityMisconfigChallenge2Fragment.java** - FLAG_PART1 = "OWASP{" → "KEY{"
12. **SecurityMisconfigChallenge3Fragment.java** - FLAG_PART1 = "OWASP{" → "KEY{"
13. **InsufficientCryptoLessonFragment.java** - FLAG_P1 = "OWASP{" → "KEY{"
14. **InsufficientCryptoChallengeFragment.java** - ENC_PART1 byte array (79,87,65,83,80,123) → (75,69,89,123)
15. **InsecureCommChallengeFragment.java** - ENC_PART1 byte array (79,87,65,83,80) → (75,69,89)
16. **FlagValidator.java** - Updated all 26 SHA-256 hashes, changed comment from "OWASP{} format" to "KEY{} format"

### Test Files Updated (9 test files)
1. LessonFragmentTest.java
2. InputValidationLessonTest.java
3. InsecureAuthorizationLessonTest.java
4. InsufficientCryptoLessonTest.java
5. PrivacyControlsLessonTest.java
6. PoorAuthLessonTest.java
7. ReverseEngineering2Test.java
8. ReverseEngineeringChallenge1Test.java
9. ReverseEngineeringChallenge3Test.java

## ASCII Reference
For byte array conversions:
- `K` = 75
- `E` = 69
- `Y` = 89
- `{` = 123
- `}` = 125

Therefore:
- `KEY{` = `{75, 69, 89, 123}`
- `OWASP{` = `{79, 87, 65, 83, 80, 123}`

## Verification
All changes have been tested and verified:
- ✅ Build successful (assembleDebug)
- ✅ All 26 SHA-256 hashes regenerated
- ✅ FlagValidator.java updated with new hashes
- ✅ Test files updated to expect KEY{} format
- ✅ No compilation errors

## Migration Date
April 1, 2026

## Previous Formats (Deprecated)
- ❌ OWASP{} - Used in original implementation
- ❌ FLAG{} - Used in 4 files
- ❌ Shepherd{} - Used in 1 file

All deprecated formats have been converted to **KEY{}**.
