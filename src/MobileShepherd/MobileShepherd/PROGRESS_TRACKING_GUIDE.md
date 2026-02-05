# Progress Tracking & Flag Validation System

## Overview

This document explains the hash-based flag validation and progress tracking system implemented in MobileShepherd.

## Architecture

### 1. FlagValidator.java
Central utility class that validates flags using SHA-256 hashing.

**Key Features:**
- Stores SHA-256 hashes of all correct flags
- Validates submissions without exposing plaintext flags
- Enum-based module identification
- Supports both lessons and challenges

**Usage Example:**
```java
boolean isValid = FlagValidator.validateFlag(
    FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1,
    userSubmittedFlag
);
```

### 2. ProgressTracker.java
Manages user progress through challenges and lessons using SharedPreferences.

**Tracks:**
- Which modules are completed
- First completion timestamp
- Last completion timestamp
- Number of times completed (for practice)

**Usage Example:**
```java
ProgressTracker tracker = new ProgressTracker(context);

// Mark complete
tracker.markCompleted(FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1);

// Check status
boolean completed = tracker.isCompleted(FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1);

// Get stats
int totalCompleted = tracker.getTotalCompletedCount();
int percentage = tracker.getCompletionPercentage();
```

### 3. ProgressFragment.java
Visual dashboard showing user progress.

**Displays:**
- Overall completion percentage
- Challenges completed (X / 15)
- Lessons completed (X / 11)
- Detailed list of all modules with status
- Completion dates and repeat counts

## Security Considerations

### Why Hashing is Appropriate

1. **Android Apps are Decompilable:** APKs can always be reverse-engineered, so hiding flags in plaintext vs hashes provides minimal security benefit.

2. **Purpose:** The goal is education, not impenetrable security. Hashing:
   - Keeps code cleaner (no scattered plaintext flags)
   - Provides consistent validation mechanism
   - Slightly increases effort for casual cheaters
   - Enables centralized flag management

3. **Determined Attackers:** Someone skilled enough to extract flags from hashes likely doesn't need to cheat on beginner mobile security challenges.

### Hash Algorithm: SHA-256

**Why SHA-256?**
- Fast computation
- Standard Java library support (MessageDigest)
- 256-bit output (64 hex characters)
- No collisions for our use case
- One-way function (can't reverse)

**Not Using:**
- **bcrypt/Argon2:** Overkill for this use case, designed for password storage with intentional slowness
- **MD5:** Deprecated, has collision vulnerabilities
- **Plain Base64:** Not a hash, easily reversible

## Implementation Guide

### Step 1: Update Challenge ViewModels

Replace plaintext validation with hash-based:

**Before:**
```java
public class Challenge1Model extends ViewModel {
    private static final String CORRECT_FLAG = "SourHatsAndAngryCats";
    
    public boolean validateFlag(String flag) {
        return flag != null && flag.trim().equals(CORRECT_FLAG);
    }
}
```

**After:**
```java
public class Challenge1Model extends ViewModel {
    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(
            FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1,
            flag
        );
    }
}
```

### Step 2: Update Fragments to Track Progress

Add progress tracking to flag submission:

```java
public class Challenge1Fragment extends Fragment {
    private ProgressTracker progressTracker;
    
    @Override
    public View onCreateView(...) {
        // ... existing code ...
        progressTracker = new ProgressTracker(requireContext());
    }
    
    private void submitFlag() {
        String flag = binding.flagInput.getText().toString();
        
        if (viewModel.validateFlag(flag)) {
            // Mark as completed
            progressTracker.markCompleted(
                FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1
            );
            
            // Show success with progress
            int completionCount = progressTracker.getCompletionCount(
                FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1
            );
            
            showSuccessDialog(
                "Flag: " + flag + "\n" +
                "Progress: " + progressTracker.getCompletedChallengesCount() + 
                "/" + progressTracker.getTotalChallengesCount()
            );
        }
    }
}
```

### Step 3: Add Progress Dashboard

Add to navigation and menu:

**mobile_navigation.xml:**
```xml
<fragment
    android:id="@+id/nav_progress"
    android:name="com.owasp.app.ui.progress.ProgressFragment"
    android:label="My Progress"
    tools:layout="@layout/fragment_progress" />
```

**activity_main_drawer.xml:**
```xml
<item
    android:id="@+id/nav_progress"
    android:icon="@drawable/ic_menu_trophy"
    android:title="My Progress" />
```

## Generating New Flag Hashes

When adding new challenges:

### Option 1: Python Script
```bash
python flag_hash_generator.py
```

### Option 2: In-App Helper
```java
// Use for testing/development only
String hash = FlagValidator.generateHash("your_new_flag_here");
Log.d("FLAG_HASH", "Hash: " + hash);
```

### Option 3: Command Line
```bash
# Linux/Mac
echo -n "your_flag_here" | sha256sum

# Windows PowerShell
[System.BitConverter]::ToString(
  [System.Security.Cryptography.SHA256]::Create().ComputeHash(
    [System.Text.Encoding]::UTF8.GetBytes("your_flag_here")
  )
).Replace("-","").ToLower()
```

## Data Storage

### SharedPreferences Structure

**File:** `MobileShepherd_Progress.xml` (in app private storage)

```xml
<?xml version="1.0" encoding="utf-8"?>
<map>
    <!-- Set of completed module IDs -->
    <set name="completed_modules">
        <string>re_challenge_1</string>
        <string>client_side_injection_challenge_1</string>
    </set>
    
    <!-- First completion timestamps -->
    <long name="first_completion_re_challenge_1" value="1706918400000" />
    <long name="first_completion_client_side_injection_challenge_1" value="1706918500000" />
    
    <!-- Last completion timestamps -->
    <long name="last_completion_re_challenge_1" value="1706918400000" />
    <long name="last_completion_client_side_injection_challenge_1" value="1706925000000" />
    
    <!-- Completion counts -->
    <int name="completion_count_re_challenge_1" value="1" />
    <int name="completion_count_client_side_injection_challenge_1" value="3" />
</map>
```

### Resetting Progress

**Option 1: In-App**
```java
progressTracker.resetProgress(); // Reset all
progressTracker.resetModule(Module.RE_CHALLENGE_1); // Reset specific
```

**Option 2: Manually**
Clear app data in Android settings or delete SharedPreferences file:
```bash
adb shell run-as com.owasp.app
cd shared_prefs/
rm MobileShepherd_Progress.xml
```

## Future Enhancements

### 1. Visual Indicators
Add checkmarks to menu items for completed modules:
```java
// In MainActivity or BaseActivity
private void updateMenuWithProgress() {
    ProgressTracker tracker = new ProgressTracker(this);
    Menu menu = navigationView.getMenu();
    
    for (int i = 0; i < menu.size(); i++) {
        MenuItem item = menu.getItem(i);
        String moduleId = getModuleIdForMenuItem(item.getItemId());
        
        if (tracker.isCompleted(getModuleFromId(moduleId))) {
            item.setIcon(R.drawable.ic_check_circle);
        }
    }
}
```

### 2. Achievements/Badges
```java
public class AchievementTracker {
    // "Speed Runner" - Complete challenge in under 5 minutes
    // "Perfectionist" - 100% completion
    // "Persistent" - Complete same challenge 5 times
    // "Security Expert" - Complete all hard challenges
}
```

### 3. Leaderboard Integration
Connect to Security Shepherd web app for global rankings:
```java
public class LeaderboardSync {
    public void syncProgress(String username, ProgressData data) {
        // POST to Security Shepherd API
    }
}
```

### 4. Export/Import Progress
```java
public String exportProgress() {
    // Export as JSON for backup
}

public void importProgress(String json) {
    // Import from backup
}
```

## Testing

### Unit Tests

**FlagValidatorTest.java:**
```java
@Test
public void testValidFlag() {
    assertTrue(FlagValidator.validateFlag(
        Module.CLIENT_SIDE_INJECTION_CHALLENGE_1,
        "SourHatsAndAngryCats"
    ));
}

@Test
public void testInvalidFlag() {
    assertFalse(FlagValidator.validateFlag(
        Module.CLIENT_SIDE_INJECTION_CHALLENGE_1,
        "WrongFlag"
    ));
}
```

**ProgressTrackerTest.java:**
```java
@Test
public void testMarkCompleted() {
    ProgressTracker tracker = new ProgressTracker(context);
    tracker.markCompleted(Module.RE_CHALLENGE_1);
    
    assertTrue(tracker.isCompleted(Module.RE_CHALLENGE_1));
    assertEquals(1, tracker.getTotalCompletedCount());
}
```

## Troubleshooting

### Issue: Progress Not Saving
**Solution:** Check SharedPreferences permissions and app data directory.

```bash
adb shell run-as com.owasp.app
cd shared_prefs/
ls -la
cat MobileShepherd_Progress.xml
```

### Issue: Flag Always Invalid
**Solution:** Check for whitespace, case sensitivity, and verify hash generation.

```java
String submitted = "SourHatsAndAngryCats";
String hash = FlagValidator.generateHash(submitted);
Log.d("DEBUG", "Submitted: " + submitted);
Log.d("DEBUG", "Hash: " + hash);
```

### Issue: Progress Reset After Update
**Solution:** SharedPreferences persist across app updates unless you explicitly clear them or use a different app signature.

## References

- SHA-256 Specification: [FIPS 180-4](https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.180-4.pdf)
- Android SharedPreferences: [Developer Guide](https://developer.android.com/reference/android/content/SharedPreferences)
- Security Best Practices: [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
