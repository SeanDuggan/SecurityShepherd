package com.owasp.app;

import com.owasp.app.ui.lessons.LessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Unit tests for LessonFragment - testing embedded flag constant
 */
public class LessonFragmentTest {

    @Test
    public void testHiddenFlagExists() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        Object flagValue = flagField.get(null);
        
        assertNotNull("HIDDEN_FLAG should exist", flagValue);
        assertTrue("HIDDEN_FLAG should be a String", flagValue instanceof String);
    }

    @Test
    public void testHiddenFlagFormat() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertTrue("Flag should start with FLAG{", flag.startsWith("FLAG{"));
        assertTrue("Flag should end with }", flag.endsWith("}"));
        assertTrue("Flag should contain 'R3v3rs3'", flag.contains("R3v3rs3"));
    }

    @Test
    public void testHiddenFlagIsNotEmpty() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertFalse("Flag should not be empty", flag.isEmpty());
        assertTrue("Flag should be at least 10 characters", flag.length() >= 10);
    }

    @Test
    public void testHiddenFlagValue() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertEquals("Flag should match expected value", 
                     "FLAG{R3v3rs3_Eng1n33r1ng_M4st3r_2024}", flag);
    }
}
