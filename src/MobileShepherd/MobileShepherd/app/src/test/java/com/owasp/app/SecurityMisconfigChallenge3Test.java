package com.owasp.app;

import com.owasp.app.ui.challenges.securitymisconfig.SecurityMisconfigChallenge3Fragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for Security Misconfiguration Challenge 3
 * Tests advanced security configuration vulnerabilities
 */
public class SecurityMisconfigChallenge3Test {

    @Test
    public void testClassExists() {
        assertNotNull("SecurityMisconfigChallenge3Fragment class should exist", 
                     SecurityMisconfigChallenge3Fragment.class);
    }

    @Test
    public void testOnCreateViewMethodExists() throws Exception {
        Method method = SecurityMisconfigChallenge3Fragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testShowVulnerabilityInfoMethodExists() throws Exception {
        Method method = SecurityMisconfigChallenge3Fragment.class.getDeclaredMethod("showVulnerabilityInfo");
        assertNotNull("showVulnerabilityInfo method should exist", method);
    }

    @Test
    public void testOnDestroyViewMethodExists() throws Exception {
        Method method = SecurityMisconfigChallenge3Fragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }

    @Test
    public void testFragmentExtendsFragment() {
        assertTrue("Should extend Fragment", 
            androidx.fragment.app.Fragment.class.isAssignableFrom(SecurityMisconfigChallenge3Fragment.class));
    }
}
