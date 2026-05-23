package com.owasp.app.ui.lessons.securitymisconfig;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.owasp.app.R;
import com.owasp.app.utils.FlagProvider;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

public class MisconfigLessonSecretActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_misconfig_lesson_secret);
        
        // Mark lesson as complete when exploited component is accessed
        ProgressTracker progressTracker = new ProgressTracker(this);
        progressTracker.markCompleted(FlagValidator.Module.SECURITY_MISCONFIG_LESSON);
        
        TextView keyText = findViewById(R.id.secret_key_text);
        TextView messageText = findViewById(R.id.secret_message_text);
        
        keyText.setText("Loading...");
        FlagProvider.getFlag(this, FlagValidator.Module.SECURITY_MISCONFIG_LESSON,
                flagValue -> {
                    keyText.setText(flagValue);
                    FlagValidator.validateFlag(MisconfigLessonSecretActivity.this,
                            FlagValidator.Module.SECURITY_MISCONFIG_LESSON,
                            flagValue,
                            correct -> android.util.Log.d("MisconfigLesson", "Server submission: " + correct));
                });
        
        String message = "⚠️ SECURITY VULNERABILITY EXPLOITED!\n\n" +
                "This activity was marked as 'exported=\"true\"' in AndroidManifest.xml, " +
                "allowing ANY app (or ADB command) to invoke it without permission checks.\n\n" +
                "Attacker Command Used:\n" +
                "adb shell am start -n com.owasp.app/.ui.lessons.securitymisconfig.MisconfigLessonSecretActivity\n\n" +
                "In a real app, this could expose:\n" +
                "• Administrative functions\n" +
                "• Password reset screens\n" +
                "• Data export utilities\n" +
                "• Debug/testing features\n\n" +
                "FIX: Set android:exported=\"false\" or add proper permission requirements!\n\n" +
                "✓ Lesson automatically marked as complete!";
        
        messageText.setText(message);
        
        Toast.makeText(this, "✓ Security Misconfiguration Lesson Complete!", Toast.LENGTH_LONG).show();
    }
}
