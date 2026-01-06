package com.owasp.app;

import com.owasp.app.ui.lessons.insecureauthorization.InsecureAuthorizationLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Unit tests for InsecureAuthorizationLessonFragment - testing demo credentials and admin flag
 */
public class InsecureAuthorizationLessonTest {

    @Test
    public void testDemoUsernameExists() throws Exception {
        Field usernameField = InsecureAuthorizationLessonFragment.class.getDeclaredField("DEMO_USERNAME");
        usernameField.setAccessible(true);
        String username = (String) usernameField.get(null);
        
        assertNotNull("Demo username should exist", username);
        assertFalse("Username should not be empty", username.isEmpty());
        assertEquals("Username should be 'testuser'", "testuser", username);
    }

    @Test
    public void testDemoPasswordExists() throws Exception {
        Field passwordField = InsecureAuthorizationLessonFragment.class.getDeclaredField("DEMO_PASSWORD");
        passwordField.setAccessible(true);
        String password = (String) passwordField.get(null);
        
        assertNotNull("Demo password should exist", password);
        assertFalse("Password should not be empty", password.isEmpty());
        assertEquals("Password should be 'password123'", "password123", password);
    }

    @Test
    public void testAdminFlagExists() throws Exception {
        Field flagField = InsecureAuthorizationLessonFragment.class.getDeclaredField("ADMIN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertNotNull("Admin flag should exist", flag);
        assertTrue("Flag should start with Shepherd{", flag.startsWith("Shepherd{"));
        assertTrue("Flag should end with }", flag.endsWith("}"));
    }

    @Test
    public void testAdminFlagValue() throws Exception {
        Field flagField = InsecureAuthorizationLessonFragment.class.getDeclaredField("ADMIN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertEquals("Flag should match expected value", 
                     "Shepherd{Pr1v1l3g3_Esc4l4t10n_Pwn3d}", flag);
    }

    @Test
    public void testPrefsNameConstant() throws Exception {
        Field prefsField = InsecureAuthorizationLessonFragment.class.getDeclaredField("PREFS_NAME");
        prefsField.setAccessible(true);
        String prefsName = (String) prefsField.get(null);
        
        assertEquals("SharedPreferences name should be 'UserSession'", "UserSession", prefsName);
    }

    @Test
    public void testFlagContainsPrivilegeEscalation() throws Exception {
        Field flagField = InsecureAuthorizationLessonFragment.class.getDeclaredField("ADMIN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        // Check for privilege escalation references (including leetspeak variants)
        String flagLower = flag.toLowerCase().replaceAll("[0-9]", "");
        assertTrue("Flag should reference privilege escalation", 
                   flagLower.contains("priv") || flagLower.contains("escal") || 
                   flag.contains("Pr1v1l3g3") || flag.contains("Esc4l4t10n"));
    }
}
