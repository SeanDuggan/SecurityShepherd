# Mobile Security Shepherd - Development Guidelines

## Core Philosophy

This application is a **Capture The Flag (CTF)** style mobile security training platform, NOT a step-by-step tutorial system. The goal is to demonstrate real OWASP Mobile Top 10 vulnerabilities that students must discover and exploit on their own.

## Design Principles

### ✅ DO:
- **Present real, exploitable vulnerabilities** that mirror actual security issues
- **Let students discover vulnerabilities** through reconnaissance, analysis, and testing
- **Provide minimal hints** - only point students in the right direction
- **Use realistic scenarios** based on documented CVEs and real-world incidents
- **Make exploitation require skill** - reverse engineering, traffic analysis, code review
- **Reward creative thinking** and multiple solution paths
- **Teach through doing** - students learn by exploiting, not by following instructions

### ❌ DON'T:
- **Avoid step-by-step tutorials** - no "Step 1, Step 2, Step 3" walkthroughs
- **Don't hold hands** - students should struggle and research
- **No progressive disclosure** - don't unlock sections after completing steps
- **Avoid explicit instructions** like "Enter the vulnerable dependency name here"
- **Don't duplicate vulnerability types** - too many lessons use "find hardcoded key in logcat"
- **Avoid tutorial language** - no "Now that you've completed Step 1..."
- **Don't use emojis** - keep it professional

## Challenge Design Pattern

### Good CTF Challenge Structure:
```
1. Brief scenario/context (1-2 sentences)
2. Objective (what to find/exploit)
3. Minimal hints (optional, subtle)
4. Validation mechanism (flag submission)
```

### Bad Tutorial Structure:
```
❌ Step 1: Do this specific thing
❌ Step 2: Now click here and enter this
❌ Step 3: Congratulations, here's what you learned
```

## Example: Good vs Bad

### ❌ BAD (Tutorial Style):
```
Step 1: Identify Dependencies
- Look at the dependencies listed below
- Enter the vulnerable dependency name
[Input field with validation]

Step 2: Research the CVE
- The vulnerability is CVE-2023-XXXX
- Click here to learn about it
[Unlocks after Step 1]

Step 3: Extract the key
- Check logcat for the API key
- Enter it here to get the flag
```

### ✅ GOOD (CTF Style):
```
This app uses a vulnerable third-party analytics SDK.
Find and exploit the vulnerability to obtain the flag.

Hint: Check the dependency versions and CVE databases.

[Single flag input field]
```

## OWASP Mobile Top 10 Mapping

Each lesson/challenge should demonstrate ONE specific OWASP risk:

- **M1: Improper Platform Usage** - Misuse of platform features or security controls
- **M2: Insecure Data Storage** - Unencrypted storage, SQLite, SharedPreferences
- **M3: Insecure Communication** - Weak TLS, cleartext traffic, certificate issues
- **M4: Insecure Authentication** - Weak passwords, client-side auth, session management
- **M5: Insufficient Cryptography** - Weak algorithms, hardcoded keys, improper implementation
- **M6: Insecure Authorization** - Client-side authorization, privilege escalation
- **M7: Client Code Quality** - Buffer overflows, format strings, code injection
- **M8: Code Tampering** - Lack of binary protections, runtime manipulation
- **M9: Reverse Engineering** - Lack of obfuscation, exposed secrets in code
- **M10: Extraneous Functionality** - Debug code, backdoors, hidden functionality

## Vulnerability Variety

### Current Issues:
- Too many lessons use: "Find hardcoded key in logcat or decompiled code"
- Need more variety in exploitation techniques

### Desired Variety:
- **Static Analysis**: Decompile APK, read source code, find hardcoded secrets
- **Dynamic Analysis**: Traffic interception, runtime hooking, behavior monitoring
- **Binary Analysis**: Analyze native libraries, examine binary protections
- **Privilege Escalation**: Exploit authorization flaws, access restricted features
- **Input Validation**: SQL injection, XSS, path traversal, format strings
- **Cryptographic Attacks**: Weak encryption, algorithm downgrade, key extraction
- **Configuration Issues**: Manifest analysis, exported components, debug flags
- **Logic Flaws**: Business logic bypass, race conditions, state manipulation

## Real-World Vulnerability Examples

When designing challenges, base them on actual incidents:

### Good Sources:
- **CVE Databases**: Use real CVE numbers and vulnerability descriptions
- **Security Research**: OWASP reports, vendor advisories, security blogs
- **Incident Reports**: Notable breaches, malware campaigns, supply chain attacks

### Examples of Real Incidents to Model:
- Firebase exposed API keys (ongoing, common)
- Twilio hardcoded credentials ($10K+ fraud incidents)
- OkHttp certificate pinning bypass (CVE-2016-2402)
- ZXing intent hijacking (URL spoofing attacks)
- Crashlytics PII leakage (2017-2018)
- Branch.io deep link exploitation (2019-2020)

## User Interface Guidelines

### Lesson/Challenge UI Should:
- **Show minimal context** - brief description of the scenario
- **Display hints sparingly** - only when truly stuck
- **Provide flag submission** - simple input field for the answer
- **Give feedback** - success/failure messages
- **Link to OWASP** - reference the specific Top 10 risk

### Avoid:
- Long explanatory text before attempting
- Multiple input fields for "steps"
- Progress indicators showing completion percentage
- Educational content that teaches before exploiting
- "Interactive demos" that walk through the solution

## Flag Format

**Standard format**: `KEY{Descriptive_Text_Here}`

**Examples**:
- `KEY{Vuln3r4bl3_D3p3nd3ncy}`
- `KEY{SQL_1nj3ct10n_Pwn}`
- `KEY{3x1f_M3t4d4t4_L34k5}`

## Testing Checklist

Before finalizing a lesson/challenge:

- [ ] Can be solved WITHOUT following steps?
- [ ] Requires actual exploitation skills?
- [ ] Based on real-world vulnerability?
- [ ] Different technique from other lessons?
- [ ] Minimal hand-holding?
- [ ] No emoji usage?
- [ ] Properly linked to OWASP risk?
- [ ] Flag format correct?
- [ ] Playable like a CTF challenge?

## Mobile-Specific Considerations

### Tools Students Should Use:
- **APKTool**: Decompile and recompile APKs
- **JADX**: View decompiled Java/Kotlin source
- **ADB**: Android Debug Bridge for device interaction
- **Logcat**: View application logs
- **Burp Suite/mitmproxy**: Intercept network traffic
- **Frida**: Runtime instrumentation and hooking
- **MobSF**: Static and dynamic analysis
- **SQLite Browser**: Examine databases

### Skills Students Should Learn:
- APK reverse engineering
- Manifest analysis
- Intent exploitation
- SQLite database inspection
- Network traffic analysis
- Certificate pinning bypass
- Runtime memory inspection
- Native library analysis

## Architecture Notes

### FAB (Floating Action Button) System:
- **Main FAB**: Help/info icon
- **Mini FAB 1 (Blue)**: Lesson information dialog
- **Mini FAB 2 (Orange)**: Link to OWASP Mobile Top 10
- **Mini FAB 3 (Green/Red)**: Mark complete/incomplete toggle
  - Green when incomplete (will mark as complete)
  - Red when complete (will mark as incomplete)

### Progress Tracking:
- Completed lessons/challenges disappear from main menu
- Appear only in collapsible "Completed" section
- Encourages progression through content

### String Resources:
- No emojis in any user-facing text
- Professional, technical language
- Brief descriptions, not tutorials

## Summary

**Remember**: This is a **mobile penetration testing CTF**, not a guided learning platform. Students should feel like hackers discovering and exploiting real vulnerabilities, not students following a tutorial.

**The best challenge is one where**:
- The vulnerability exists and is exploitable
- Students must research and discover how
- Multiple approaches may work
- It mirrors a real-world security issue
- Success feels like a genuine achievement

