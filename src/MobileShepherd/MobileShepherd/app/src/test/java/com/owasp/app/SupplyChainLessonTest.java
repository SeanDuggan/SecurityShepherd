package com.owasp.app;

import com.owasp.app.ui.lessons.supplychain.SupplyChainLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Unit tests for SupplyChainLessonFragment - testing vulnerable third-party API key and flag
 */
public class SupplyChainLessonTest {

    @Test
    public void testThirdPartyApiKeyExists() throws Exception {
        Field apiKeyField = SupplyChainLessonFragment.class.getDeclaredField("THIRD_PARTY_API_KEY");
        apiKeyField.setAccessible(true);
        String apiKey = (String) apiKeyField.get(null);
        
        assertNotNull("Third-party API key should exist", apiKey);
        assertFalse("API key should not be empty", apiKey.isEmpty());
    }

    @Test
    public void testThirdPartyApiKeyValue() throws Exception {
        Field apiKeyField = SupplyChainLessonFragment.class.getDeclaredField("THIRD_PARTY_API_KEY");
        apiKeyField.setAccessible(true);
        String apiKey = (String) apiKeyField.get(null);
        
        assertEquals("API key should match expected value", 
                     "sk_live_vulnerable_key_12345", apiKey);
    }

    @Test
    public void testApiKeyFormat() throws Exception {
        Field apiKeyField = SupplyChainLessonFragment.class.getDeclaredField("THIRD_PARTY_API_KEY");
        apiKeyField.setAccessible(true);
        String apiKey = (String) apiKeyField.get(null);
        
        assertTrue("API key should start with 'sk_live_'", apiKey.startsWith("sk_live_"));
    }

    @Test
    public void testDemoFlagExists() throws Exception {
        Field flagField = SupplyChainLessonFragment.class.getDeclaredField("DEMO_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertNotNull("DEMO_FLAG should exist", flag);
        assertTrue("Flag should start with OWASP{", flag.startsWith("OWASP{"));
        assertTrue("Flag should end with }", flag.endsWith("}"));
    }

    @Test
    public void testDemoFlagValue() throws Exception {
        Field flagField = SupplyChainLessonFragment.class.getDeclaredField("DEMO_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertEquals("Flag should match expected value", 
                     "OWASP{Vuln3r4bl3_D3p3nd3ncy}", flag);
    }

    @Test
    public void testFlagRelatesToDependency() throws Exception {
        Field flagField = SupplyChainLessonFragment.class.getDeclaredField("DEMO_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertTrue("Flag should reference vulnerability or dependency", 
                   flag.toLowerCase().contains("vuln") || flag.toLowerCase().contains("depend"));
    }
}
