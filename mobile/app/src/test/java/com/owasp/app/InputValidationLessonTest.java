package com.owasp.app;

import com.owasp.app.ui.lessons.InputValidationLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Unit tests for InputValidationLessonFragment - testing validation bypass flag
 */
public class InputValidationLessonTest {

    @Test
    public void testHiddenFlagExists() throws Exception {
        Field flagField = InputValidationLessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertNotNull("HIDDEN_FLAG should exist", flag);
        assertTrue("Flag should start with KEY{", flag.startsWith("KEY{"));
        assertTrue("Flag should end with }", flag.endsWith("}"));
    }

    @Test
    public void testHiddenFlagValue() throws Exception {
        Field flagField = InputValidationLessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertEquals("Flag should match expected value", 
                     "KEY{1nput_V4l1d4t10n_Byp4ss3d}", flag);
    }

    @Test
    public void testFlagContainsInputValidation() throws Exception {
        Field flagField = InputValidationLessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertTrue("Flag should reference input validation or bypass", 
                   flag.toLowerCase().contains("input") || 
                   flag.toLowerCase().contains("valid") || 
                   flag.toLowerCase().contains("byp"));
    }

    @Test
    public void testFlagIsNotEmpty() throws Exception {
        Field flagField = InputValidationLessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertFalse("Flag should not be empty", flag.isEmpty());
        assertTrue("Flag should be at least 15 characters", flag.length() >= 15);
    }
}
