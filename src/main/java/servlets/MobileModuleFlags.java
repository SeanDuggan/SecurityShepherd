package servlets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Server-side base flag values for mobile modules.
 *
 * <p>These plaintext strings are <strong>never</strong> sent to the Android client. Only their
 * HMAC — keyed with the server's ephemeral key and the authenticated user's name — is returned via
 * {@code MobileFlagGet}. This ensures every student receives a unique flag that cannot be
 * precomputed by decompiling the APK.
 *
 * <p>Add an entry here when a new mobile module is promoted to server-side validation.
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */
final class MobileModuleFlags {

  private MobileModuleFlags() {}

  /**
   * Maps mobile module ID strings to their corresponding database moduleId UUIDs.
   * Used by MobileFlagSubmit to record completion and award points via Setter.updatePlayerResult.
   */
  static final Map<String, String> MODULE_DB_IDS;

  static final Map<String, String> BASE_FLAGS;

  static {
    Map<String, String> m = new HashMap<>();
    // Client-Side Injection (M7 / SQLite)
    m.put("client_side_injection_lesson", "KEY{CL13NT_S1D3_SQL_1NJ3CT10N}");
    // Poor Authentication (M3) — flag revealed after cracking hardcoded PIN
    m.put("poor_auth_lesson", "Taco_Snores_On_A_Couch");
    // Insecure Authorization (M3) — flag revealed after privilege escalation via SharedPreferences
    m.put("insecure_auth_lesson", "KEY{Pr1v1l3g3_Esc4l4t10n_Pwn3d}");
    // Input Validation (M4) — flag revealed after URL validation bypass
    m.put("input_validation_lesson", "KEY{1nput_V4l1d4t10n_Byp4ss3d}");
    // Supply Chain (M6) — flag obtained from vulnerable dependency debug logs
    m.put("supply_chain_lesson", "KEY{Vuln3r4bl3_D3p3nd3ncy}");
    // Reverse Engineering (M9) — flag found via static APK analysis
    m.put("re_lesson", "KEY{R3v3rs3_Eng1n33r1ng_M4st3r_2024}");
    // Security Misconfiguration (M1) — flag exposed via exported activity
    m.put("security_misconfig_lesson", "KEY{Exp0rt3d_C0mp0n3nt_Vuln3r4b1l1ty}");
    // Privacy Controls (M8) — flag embedded in image EXIF metadata
    m.put("privacy_lesson", "KEY{3x1f_M3t4d4t4_L34k5_L0c4t10n}");
    // Insecure Data Storage (M2) — flag stored as plaintext password in SQLite
    m.put("ids_lesson", "Battery777");
    // Insecure Communication (M5) — flag transmitted as HTTP API key header
    m.put("insecure_comm_lesson", "OWASP{H1TTP_Insecure_F1nd}");
    BASE_FLAGS = Collections.unmodifiableMap(m);

    Map<String, String> ids = new HashMap<>();
    // Existing mobile modules already in the platform DB
    ids.put("ids_lesson",                 "53a53a66cb3bf3e4c665c442425ca90e29536edd");
    ids.put("re_lesson",                  "2ab09c0c18470ae5f87d219d019a1f603e66f944");
    ids.put("poor_auth_lesson",           "0cdd1549e7c74084d7059ce748b93ef657b44457");
    ids.put("client_side_injection_lesson","335440fef02d19259254ed88293b62f31cccdd41");
    // New mobile-specific modules added to the platform
    ids.put("insecure_auth_lesson",       "0f40ae03b9339cb88fbd834213ee1c597791274a");
    ids.put("insecure_comm_lesson",       "a76d11ebd575aecfba5d69441cbd90c95e8abe31");
    ids.put("security_misconfig_lesson",  "c85dad7f468a333e53edaca90a435528db76d118");
    ids.put("input_validation_lesson",    "708b76213e50409e138fc68eba81ed7ec8fccf08");
    ids.put("privacy_lesson",             "952c4c3785d8bd8d51bb0d0161c3f6997dd01863");
    MODULE_DB_IDS = Collections.unmodifiableMap(ids);
  }
}
