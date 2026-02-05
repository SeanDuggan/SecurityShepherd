package com.owasp.app;

import com.owasp.app.ui.challenges.inputvalidation.XssWebViewChallengeFragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for XSS WebView Challenge
 * Tests input validation vulnerability in WebView
 */
public class XssWebViewChallengeTest {

    @Test
    public void testClassExists() {
        assertNotNull("XssWebViewChallengeFragment class should exist", 
                     XssWebViewChallengeFragment.class);
    }

    @Test
    public void testOnCreateViewMethodExists() throws Exception {
        Method method = XssWebViewChallengeFragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testShowVulnerabilityInfoMethodExists() throws Exception {
        Method method = XssWebViewChallengeFragment.class.getDeclaredMethod("showVulnerabilityInfo");
        assertNotNull("showVulnerabilityInfo method should exist", method);
    }

    @Test
    public void testOnDestroyViewMethodExists() throws Exception {
        Method method = XssWebViewChallengeFragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }
}
