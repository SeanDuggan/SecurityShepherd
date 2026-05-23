package com.owasp.app.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the correct flag string for each mobile module in both offline and online modes.
 *
 * <p><b>Offline mode</b> — when no server credentials are configured, a static plaintext flag is
 * returned immediately. These values can be read from the APK, which is acceptable for offline
 * (unauthenticated) training use.
 *
 * <p><b>Online mode</b> — when the student is signed in, the server-generated user-specific flag
 * is fetched from {@code /mobileFlagGet}. This flag is an HMAC of the base flag keyed with the
 * server's ephemeral key and the student's username — unique per user, per server session, and
 * unreachable via APK inspection.
 *
 * <p>In online mode the challenge DB is seeded with the dynamic flag before the student begins
 * searching, so the value they discover via exploitation is always the server-validated flag.
 */
public class FlagProvider {

    private static final String TAG = "FlagProvider";

    /**
     * Offline (static) flag values — returned when no server is configured.
     * Mirrors the base flags in the server's {@code MobileModuleFlags} class.
     */
    private static final Map<FlagValidator.Module, String> OFFLINE_FLAGS = new HashMap<>();

    static {
        OFFLINE_FLAGS.put(
                FlagValidator.Module.CLIENT_SIDE_INJECTION_LESSON,
                "KEY{CL13NT_S1D3_SQL_1NJ3CT10N}");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.POOR_AUTH_LESSON,
                "Taco_Snores_On_A_Couch");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.INSECURE_AUTH_LESSON,
                "KEY{Pr1v1l3g3_Esc4l4t10n_Pwn3d}");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.INPUT_VALIDATION_LESSON,
                "KEY{1nput_V4l1d4t10n_Byp4ss3d}");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.SUPPLY_CHAIN_LESSON,
                "KEY{Vuln3r4bl3_D3p3nd3ncy}");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.RE_LESSON,
                "KEY{R3v3rs3_Eng1n33r1ng_M4st3r_2024}");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.SECURITY_MISCONFIG_LESSON,
                "KEY{Exp0rt3d_C0mp0n3nt_Vuln3r4b1l1ty}");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.PRIVACY_LESSON,
                "KEY{3x1f_M3t4d4t4_L34k5_L0c4t10n}");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.IDS_LESSON,
                "Battery777");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.INSECURE_COMM_LESSON,
                "OWASP{H1TTP_Insecure_F1nd}");
        OFFLINE_FLAGS.put(
                FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON,
                "KEY{DES_Encrypt10n}");
    }

    public interface FlagCallback {
        /** Always invoked on the main thread. */
        void onFlag(String flag);
    }

    /**
     * Returns the flag for the given module.
     *
     * <ul>
     *   <li>If the student is authenticated ({@link AuthManager#isAuthenticated}), a background
     *       HTTP request fetches the user-specific dynamic flag. On network failure the offline
     *       flag is returned as a fallback so the lesson remains usable.
     *   <li>Otherwise the offline static flag is returned synchronously (still via the main thread
     *       for API consistency).
     * </ul>
     *
     * @param ctx      Context used to read credentials.
     * @param module   The module whose flag is needed.
     * @param callback Receives the flag string on the main thread.
     */
    public static void getFlag(Context ctx, FlagValidator.Module module, FlagCallback callback) {
        String offlineFlag = OFFLINE_FLAGS.containsKey(module) ? OFFLINE_FLAGS.get(module) : "";

        if (!AuthManager.isAuthenticated(ctx)) {
            Log.d(TAG, "Offline mode — returning static flag for " + module.getId());
            new Handler(Looper.getMainLooper()).post(() -> callback.onFlag(offlineFlag));
            return;
        }

        String serverUrl = AuthManager.getServerUrl(ctx);
        String login     = AuthManager.getUsername(ctx);
        String pwd       = AuthManager.getPassword(ctx);
        String endpoint  = serverUrl.replaceAll("/+$", "") + "/mobileFlagGet";
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String resultFlag = offlineFlag;
            HttpURLConnection conn = null;
            try {
                String body = "login="    + URLEncoder.encode(login, "UTF-8")
                        + "&pwd="      + URLEncoder.encode(pwd, "UTF-8")
                        + "&moduleId=" + URLEncoder.encode(module.getId(), "UTF-8");

                conn = (HttpURLConnection) new URL(endpoint).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader =
                                 new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    String fetchedFlag = json.optString("flag", "");
                    if (!fetchedFlag.isEmpty()) {
                        resultFlag = fetchedFlag;
                        Log.d(TAG, "Dynamic flag fetched for " + module.getId());
                    }
                } else {
                    Log.w(TAG, "Flag fetch HTTP " + status + " for " + module.getId()
                            + " — using offline flag");
                }
            } catch (Exception e) {
                Log.w(TAG, "Flag fetch failed for " + module.getId()
                        + ": " + e.getMessage() + " — using offline flag");
            } finally {
                if (conn != null) conn.disconnect();
            }

            final String flag = resultFlag;
            mainHandler.post(() -> callback.onFlag(flag));
        }).start();
    }
}
