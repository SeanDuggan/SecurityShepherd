package com.owasp.app;

import com.owasp.app.ui.lessons.poorauth.PoorAuthLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Unit tests for PoorAuthLessonFragment - testing hardcoded credentials and flag
 */
public class PoorAuthLessonTest {

    @Test
    public void testHardcodedPinExists() throws Exception {
        Field pinField = PoorAuthLessonFragment.class.getDeclaredField("HARDCODED_PIN");
        pinField.setAccessible(true);
        String pin = (String) pinField.get(null);
        
        assertNotNull("Hardcoded PIN should exist", pin);
        assertFalse("PIN should not be empty", pin.isEmpty());
    }

    @Test
    public void testHardcodedPinValue() throws Exception {
        Field pinField = PoorAuthLessonFragment.class.getDeclaredField("HARDCODED_PIN");
        pinField.setAccessible(true);
        String pin = (String) pinField.get(null);
        
        assertEquals("PIN should be '1234'", "1234", pin);
    }

    @Test
    public void testDemoFlagExists() throws Exception {
        Field flagField = PoorAuthLessonFragment.class.getDeclaredField("DEMO_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertNotNull("DEMO_FLAG should exist", flag);
        assertTrue("Flag should start with KEY{", flag.startsWith("KEY{"));
        assertTrue("Flag should end with }", flag.endsWith("}"));
    }

    @Test
    public void testDemoFlagValue() throws Exception {
        Field flagField = PoorAuthLessonFragment.class.getDeclaredField("DEMO_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertEquals("Flag should match expected value", 
                     "OWASP{H4rdc0d3d_PIN_Vuln}", flag);
    }

    @Test
    public void testPinIsNumeric() throws Exception {
        Field pinField = PoorAuthLessonFragment.class.getDeclaredField("HARDCODED_PIN");
        pinField.setAccessible(true);
        String pin = (String) pinField.get(null);
        
        assertTrue("PIN should be numeric", pin.matches("\\d+"));
        assertEquals("PIN should be 4 digits", 4, pin.length());
    }
}
