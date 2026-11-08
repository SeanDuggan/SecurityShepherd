package org.owasp.mobileshepherd.ui.challenges.insecurecomm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInsecureCommChallengeBinding;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.net.HttpURLConnection;
import java.net.URL;

public class InsecureCommChallengeFragment extends Fragment {

    private FragmentInsecureCommChallengeBinding binding;
    private String currentKey = "";
    private static final String TAG = "AppNetworkMonitor";
    private static final String SEPARATOR = "---------------------------------------------";
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsecureCommChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        // Fetch the server key so it is ready when simulateAppTraffic() is triggered
        KeyProvider.getKey(requireContext(), KeyValidator.Module.INSECURE_COMM_CHALLENGE, keyValue -> {
            currentKey = keyValue;
        });

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.INSECURE_COMM_CHALLENGE));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showVulnerabilityInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }

        binding.startAppButton.setOnClickListener(v -> simulateAppTraffic());
        binding.submitKeyButton.setOnClickListener(v -> submitKey());

        return root;
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = false;
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (fab2 != null) fab2.setVisibility(View.GONE);
        if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_help);
    }

    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);

        TextView introText = dialogView.findViewById(R.id.intro_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);

        introText.setText(R.string.insecure_comm_intro);

        hintsSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);

        new AlertDialog.Builder(requireContext())
                .setTitle("Insecure Communication Challenge\nOWASP M5: Insecure Communication")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void simulateAppTraffic() {
        binding.startAppButton.setEnabled(false);
        binding.trafficStatus.setText("Monitoring network traffic...");

        new Thread(() -> {
            try {
                Thread.sleep(500);
                makeSecureAnalyticsRequest();

                Thread.sleep(800);
                makeInsecureApiRequest(); // This one carries the key

                Thread.sleep(600);
                makeSecureImageRequest();

                Thread.sleep(700);
                makeInsecureMetricsRequest();

                Thread.sleep(500);
                makeSecureAuthRequest();

                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.trafficStatus.setText("[OK] Network monitoring complete!\n\n5 requests captured. Route device traffic through a proxy (e.g. mitmproxy) and inspect the plaintext HTTP request to find the key.");
                    binding.trafficStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
                    binding.submitSection.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Network monitoring complete.", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
        }).start();
    }

    private void makeSecureAnalyticsRequest() {
        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "[Request #1] Analytics Endpoint");
        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "POST https://analytics.secure-api.com/events");
        Log.d(TAG, "Protocol: HTTPS/TLS 1.3 [OK] ENCRYPTED");
        Log.d(TAG, "Headers: [ENCRYPTED]");
        Log.d(TAG, "Body: [ENCRYPTED]");
        Log.d(TAG, "Status: [OK] Secure connection established");
        Log.d(TAG, "");
    }

    private void makeInsecureApiRequest() {
        String key = currentKey.isEmpty() ? "[connect to server to load key]" : currentKey;

        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "[Request #2] User Session Endpoint");
        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "GET http://api.legacy-backend.com/user/session");
        Log.d(TAG, "Protocol: HTTP/1.1 WARNING: PLAINTEXT");
        Log.d(TAG, "");
        Log.d(TAG, "Headers:");
        Log.d(TAG, "  Host: api.legacy-backend.com");
        Log.d(TAG, "  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        Log.d(TAG, "  X-Session-Token: [REDACTED - inspect the actual request, not this log]");
        Log.d(TAG, "  User-Agent: MobileApp/2.1.4");
        Log.d(TAG, "  Accept: application/json");
        Log.d(TAG, "");
        Log.d(TAG, "SECURITY WARNING: Sensitive token sent over HTTP!");
        Log.d(TAG, "");

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://api.legacy-backend.com/user/session");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Session-Token", key);
            conn.setRequestProperty("User-Agent", "MobileApp/2.1.4");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            // Headers are only transmitted once the request is actually sent.
            conn.getResponseCode();
        } catch (Exception e) {
            Log.d(TAG, "Request finished without a usable response (expected)");
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void makeSecureImageRequest() {
        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "[Request #3] Image CDN");
        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "GET https://cdn.secure-images.com/avatar/user123.jpg");
        Log.d(TAG, "Protocol: HTTPS/TLS 1.3 [OK] ENCRYPTED");
        Log.d(TAG, "Headers: [ENCRYPTED]");
        Log.d(TAG, "Status: [OK] Secure connection established");
        Log.d(TAG, "");
    }

    private void makeInsecureMetricsRequest() {
        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "[Request #4] Metrics Endpoint");
        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "POST http://metrics.old-service.com/collect");
        Log.d(TAG, "Protocol: HTTP/1.1 WARNING: PLAINTEXT");
        Log.d(TAG, "");
        Log.d(TAG, "Headers:");
        Log.d(TAG, "  Content-Type: application/json");
        Log.d(TAG, "Body:");
        Log.d(TAG, "  {\"event\":\"app_open\",\"user_id\":\"u_847263\"}");
        Log.d(TAG, "");
        Log.d(TAG, "WARNING: Non-sensitive data, but still unencrypted");
        Log.d(TAG, "");
    }

    private void makeSecureAuthRequest() {
        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "[Request #5] Authentication Endpoint");
        Log.d(TAG, SEPARATOR);
        Log.d(TAG, "POST https://auth.secure-api.com/token/refresh");
        Log.d(TAG, "Protocol: HTTPS/TLS 1.3 [OK] ENCRYPTED");
        Log.d(TAG, "Headers: [ENCRYPTED]");
        Log.d(TAG, "Body: [ENCRYPTED]");
        Log.d(TAG, "Status: [OK] Secure connection established");
        Log.d(TAG, "");
        Log.d(TAG, "=============================================");
        Log.d(TAG, "Network capture complete");
        Log.d(TAG, "=============================================");
    }

    private void submitKey() {
        String enteredKey = binding.keyInput.getText().toString().trim();

        if (enteredKey.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a key", Toast.LENGTH_SHORT).show();
            return;
        }

        KeyValidator.validateKey(requireContext(), KeyValidator.Module.INSECURE_COMM_CHALLENGE,
                enteredKey, isValid -> {
            if (isValid) {
                progressTracker.markCompleted(KeyValidator.Module.INSECURE_COMM_CHALLENGE);

                binding.keyValidationCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success_bg));
                binding.resultText.setText("Correct key!");
                binding.resultText.setVisibility(View.VISIBLE);

                Toast.makeText(getContext(), "Challenge complete!", Toast.LENGTH_LONG).show();

                binding.submitKeyButton.setEnabled(false);
                binding.keyInput.setEnabled(false);
            } else {
                binding.keyValidationCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_bg));
                binding.resultText.setText("Incorrect. Try again.");
                binding.resultText.setVisibility(View.VISIBLE);

                Toast.makeText(getContext(), "Incorrect key. Keep analyzing!", Toast.LENGTH_SHORT).show();

                binding.keyInput.setText("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        binding = null;
    }
}
