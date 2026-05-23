package com.owasp.app.ui.scoreboard;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.owasp.app.MainActivity;
import com.owasp.app.R;
import com.owasp.app.utils.AuthManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ScoreboardFragment extends Fragment {

    private WebView webView;
    private ProgressBar progressBar;
    private View offlineView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_scoreboard, container, false);

        webView = root.findViewById(R.id.scoreboard_webview);
        progressBar = root.findViewById(R.id.scoreboard_progress);
        offlineView = root.findViewById(R.id.scoreboard_offline_view);

        MaterialButton signInButton = root.findViewById(R.id.scoreboard_sign_in_button);
        signInButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openAuthDialog();
            }
        });

        if (AuthManager.isAuthenticated(requireContext())) {
            setupWebView();
            loadWithCachedOrFreshSession();
        } else {
            offlineView.setVisibility(View.VISIBLE);
        }

        return root;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        // Fix 4: deny access to local files and content providers from the WebView
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        String serverUrl = AuthManager.getServerUrl(requireContext()).replaceAll("/+$", "");
        String serverHost = Uri.parse(serverUrl).getHost();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri requestedUri = request.getUrl();
                String requestedHost = requestedUri.getHost();

                // Fix 3: block navigation to any host other than the configured server
                if (requestedHost == null || !requestedHost.equals(serverHost)) {
                    return true; // block
                }

                // Detect session expiry — server redirects back to login.jsp
                String path = requestedUri.getPath();
                if (path != null && (path.endsWith("login.jsp") || path.endsWith("index.jsp"))) {
                    // Session expired: clear cache and re-authenticate silently
                    AuthManager.clearWebSessionCookie(requireContext());
                    CookieManager.getInstance().removeAllCookies(null);
                    loginAndLoad();
                    return true; // we handle the navigation
                }

                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) {
                    progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    /**
     * Uses the cached web session cookie if one exists; otherwise performs a fresh web login
     * (single credential send) and caches the resulting JSESSIONID for future loads.
     */
    private void loadWithCachedOrFreshSession() {
        String cached = AuthManager.getWebSessionCookie(requireContext());
        if (!cached.isEmpty()) {
            injectCookieAndLoad(cached);
        } else {
            loginAndLoad();
        }
    }

    /**
     * Performs a web login on a background thread (one credential send), caches the JSESSIONID,
     * injects it into the WebView, then loads scoreboard.jsp.
     */
    private void loginAndLoad() {
        offlineView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String serverUrl = AuthManager.getServerUrl(requireContext()).replaceAll("/+$", "");
        String username  = AuthManager.getUsername(requireContext());
        String password  = AuthManager.getPassword(requireContext());

        new Thread(() -> {
            String cookie = doWebLogin(serverUrl, username, password);
            if (!cookie.isEmpty()) {
                AuthManager.saveWebSessionCookie(requireContext(), cookie);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded()) return;
                injectCookieAndLoad(cookie);
            });
        }).start();
    }

    private void injectCookieAndLoad(String cookie) {
        String serverUrl = AuthManager.getServerUrl(requireContext()).replaceAll("/+$", "");

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (!cookie.isEmpty()) {
            for (String pair : cookie.split(";\\s*")) {
                cookieManager.setCookie(serverUrl, pair.trim());
            }
            cookieManager.flush();
        }

        offlineView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(serverUrl + "/scoreboard.jsp");
    }

    /**
     * Synchronous web login (call on a background thread). POSTs to /login with
     * {@code setInstanceFollowRedirects(false)} so we capture the Set-Cookie headers at the
     * 302 response without re-sending credentials. Returns semicolon-separated name=value cookie
     * pairs, or empty string on failure.
     */
    private String doWebLogin(String serverUrl, String username, String password) {
        HttpURLConnection conn = null;
        try {
            String body = "login=" + URLEncoder.encode(username, "UTF-8")
                    + "&pwd=" + URLEncoder.encode(password, "UTF-8");

            conn = (HttpURLConnection) new URL(serverUrl + "/login").openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);
            conn.setInstanceFollowRedirects(false); // stop at 302 to read Set-Cookie
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            conn.getResponseCode(); // complete the exchange

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
                if ("Set-Cookie".equalsIgnoreCase(header.getKey())) {
                    for (String cookieHeader : header.getValue()) {
                        // Extract name=value only — strip Path, HttpOnly, SameSite etc.
                        String nameValue = cookieHeader.split(";")[0].trim();
                        if (sb.length() > 0) sb.append("; ");
                        sb.append(nameValue);
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AuthManager.isAuthenticated(requireContext())
                && webView.getVisibility() != View.VISIBLE) {
            setupWebView();
            loadWithCachedOrFreshSession();
        }
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroyView();
    }
}
