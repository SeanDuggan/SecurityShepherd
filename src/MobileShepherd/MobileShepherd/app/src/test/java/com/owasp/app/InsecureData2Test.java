package com.owasp.app;

import com.owasp.app.ui.challenges.insecuredata2.InsecureData2Fragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for Insecure Data Storage Challenge 2
 * Tests file storage vulnerability
 */
public class InsecureData2Test {

    @Test
    public void testClassExists() {
        assertNotNull("InsecureData2Fragment class should exist", 
                     InsecureData2Fragment.class);
    }

    @Test
    public void testOnCreateViewMethodExists() throws Exception {
        Method method = InsecureData2Fragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testShowVulnerabilityInfoMethodExists() throws Exception {
        Method method = InsecureData2Fragment.class.getDeclaredMethod("showVulnerabilityInfo");
        assertNotNull("showVulnerabilityInfo method should exist", method);
    }

    @Test
    public void testOnDestroyViewMethodExists() throws Exception {
        Method method = InsecureData2Fragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }

    @Test
    public void testFragmentExtendsFragment() {
        assertTrue("Should extend Fragment", 
            androidx.fragment.app.Fragment.class.isAssignableFrom(InsecureData2Fragment.class));
    }
}
