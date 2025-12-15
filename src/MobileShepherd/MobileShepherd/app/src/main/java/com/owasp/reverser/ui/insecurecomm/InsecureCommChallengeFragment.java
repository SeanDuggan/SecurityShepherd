package com.owasp.reverser.ui.insecurecomm;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.owasp.reverser.databinding.FragmentInsecureCommChallengeBinding;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class InsecureCommChallengeFragment extends Fragment {

    private FragmentInsecureCommChallengeBinding binding;
    private InsecureCommChallengeModel viewModel;
    private static final String TAG = "AppNetworkMonitor";
    
    // Obfuscated flag components - split and encoded differently
    private static final byte[] ENC_PART1 = {79, 87, 65, 83, 80}; // OWASP
    private static final byte[] ENC_PART2 = {123, 78, 51, 116}; // {N3t
    private static final byte[] ENC_PART3 = {119, 48, 114, 107}; // w0rk
    private static final byte[] ENC_PART4 = {95, 83, 110, 49}; // _Sn1
    private static final byte[] ENC_PART5 = {102, 102, 51, 100, 125}; // ff3d}

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsecureCommChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(InsecureCommChallengeModel.class);

        binding.startAppButton.setOnClickListener(v -> simulateAppTraffic());
        binding.submitFlagButton.setOnClickListener(v -> submitFlag());

        return root;
    }

    private void simulateAppTraffic() {
        binding.startAppButton.setEnabled(false);
        binding.trafficStatus.setText("Monitoring network traffic...");
        
        new Thread(() -> {
            try {
                // Simulate multiple network requests with noise
                Thread.sleep(500);
                makeSecureAnalyticsRequest();
                
                Thread.sleep(800);
                makeInsecureApiRequest(); // This one contains the flag
                
                Thread.sleep(600);
                makeSecureImageRequest();
                
                Thread.sleep(700);
                makeInsecureMetricsRequest();
                
                Thread.sleep(500);
                makeSecureAuthRequest();
                
                requireActivity().runOnUiThread(() -> {
                    binding.trafficStatus.setText("✓ Network monitoring complete!\n\n5 requests captured. Analyze logcat to find insecure traffic.");
                    binding.trafficStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                    binding.submitSection.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Check logcat tag: AppNetworkMonitor", Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
        }).start();
    }

    private void makeSecureAnalyticsRequest() {
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #1] Analytics Endpoint");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "POST https://analytics.secure-api.com/events");
        Log.d(TAG, "Protocol: HTTPS/TLS 1.3 ✓ ENCRYPTED");
        Log.d(TAG, "Headers: [ENCRYPTED]");
        Log.d(TAG, "Body: [ENCRYPTED]");
        Log.d(TAG, "Status: ✓ Secure connection established");
        Log.d(TAG, "");
    }

    private void makeInsecureApiRequest() {
        String flag = reconstructFlag();
        
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #2] User Session Endpoint");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "GET http://api.legacy-backend.com/user/session");
        Log.d(TAG, "Protocol: HTTP/1.1 ⚠️  PLAINTEXT");
        Log.d(TAG, "");
        Log.d(TAG, "Headers:");
        Log.d(TAG, "  Host: api.legacy-backend.com");
        Log.d(TAG, "  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        Log.d(TAG, "  X-Session-Token: " + flag);
        Log.d(TAG, "  User-Agent: MobileApp/2.1.4");
        Log.d(TAG, "  Accept: application/json");
        Log.d(TAG, "");
        Log.d(TAG, "⚠️  SECURITY WARNING: Sensitive token sent over HTTP!");
        Log.d(TAG, "");
        
        try {
            URL url = new URL("http://api.legacy-backend.com/user/session");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Session-Token", flag);
            conn.setRequestProperty("User-Agent", "MobileApp/2.1.4");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            
            try {
                conn.connect();
            } catch (Exception e) {
                Log.d(TAG, "Connection failed (expected)");
            }
        } catch (Exception e) {
            // Expected to fail
        }
    }

    private void makeSecureImageRequest() {
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #3] Image CDN");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "GET https://cdn.secure-images.com/avatar/user123.jpg");
        Log.d(TAG, "Protocol: HTTPS/TLS 1.3 ✓ ENCRYPTED");
        Log.d(TAG, "Headers: [ENCRYPTED]");
        Log.d(TAG, "Status: ✓ Secure connection established");
        Log.d(TAG, "");
    }

    private void makeInsecureMetricsRequest() {
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #4] Metrics Endpoint");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "POST http://metrics.old-service.com/collect");
        Log.d(TAG, "Protocol: HTTP/1.1 ⚠️  PLAINTEXT");
        Log.d(TAG, "");
        Log.d(TAG, "Headers:");
        Log.d(TAG, "  Content-Type: application/json");
        Log.d(TAG, "Body:");
        Log.d(TAG, "  {\"event\":\"app_open\",\"user_id\":\"u_847263\"}");
        Log.d(TAG, "");
        Log.d(TAG, "⚠️  Non-sensitive data, but still unencrypted");
        Log.d(TAG, "");
    }

    private void makeSecureAuthRequest() {
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "[Request #5] Authentication Endpoint");
        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "POST https://auth.secure-api.com/token/refresh");
        Log.d(TAG, "Protocol: HTTPS/TLS 1.3 ✓ ENCRYPTED");
        Log.d(TAG, "Headers: [ENCRYPTED]");
        Log.d(TAG, "Body: [ENCRYPTED]");
        Log.d(TAG, "Status: ✓ Secure connection established");
        Log.d(TAG, "");
        Log.d(TAG, "═══════════════════════════════════");
        Log.d(TAG, "Network capture complete");
        Log.d(TAG, "═══════════════════════════════════");
    }

    private String reconstructFlag() {
        byte[] combined = new byte[ENC_PART1.length + ENC_PART2.length + ENC_PART3.length + ENC_PART4.length + ENC_PART5.length];
        int pos = 0;
        
        System.arraycopy(ENC_PART1, 0, combined, pos, ENC_PART1.length);
        pos += ENC_PART1.length;
        
        System.arraycopy(ENC_PART2, 0, combined, pos, ENC_PART2.length);
        pos += ENC_PART2.length;
        
        System.arraycopy(ENC_PART3, 0, combined, pos, ENC_PART3.length);
        pos += ENC_PART3.length;
        
        System.arraycopy(ENC_PART4, 0, combined, pos, ENC_PART4.length);
        pos += ENC_PART4.length;
        
        System.arraycopy(ENC_PART5, 0, combined, pos, ENC_PART5.length);
        
        return new String(combined, StandardCharsets.UTF_8);
    }

    private void submitFlag() {
        String enteredFlag = binding.flagInput.getText().toString().trim();

        if (enteredFlag.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a flag", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isValid = viewModel.validateFlag(enteredFlag);

        if (isValid) {
            binding.flagValidationCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            binding.resultText.setText("✓ Correct Flag!\n\nYou successfully intercepted the insecure HTTP traffic and found the session token!");
            binding.resultText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Challenge Complete!", Toast.LENGTH_LONG).show();
            
            binding.submitFlagButton.setEnabled(false);
            binding.flagInput.setEnabled(false);
        } else {
            binding.flagValidationCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            binding.resultText.setText("✗ Incorrect Flag\n\nHint: Look for HTTP (not HTTPS) requests in the logs");
            binding.resultText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Incorrect flag. Keep analyzing!", Toast.LENGTH_SHORT).show();
            
            binding.flagInput.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
