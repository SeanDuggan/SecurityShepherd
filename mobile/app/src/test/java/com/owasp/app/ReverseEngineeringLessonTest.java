package com.owasp.app;

import com.owasp.app.ui.lessons.LessonFragment;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Unit tests for Reverse Engineering Lesson
 * Tests verify that the hidden flag is properly embedded and can be extracted
 */
public class ReverseEngineeringLessonTest {

    @Test
    public void lesson_ContainsHiddenFlag() throws Exception {
        // Use reflection to access the private HIDDEN_FLAG constant
        Field flagField = LessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertNotNull("Hidden flag should not be null", flag);
        assertFalse("Hidden flag should not be empty", flag.isEmpty());
    }

    @Test
    public void lesson_FlagHasCorrectFormat() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertTrue("Flag should start with FLAG{", flag.startsWith("FLAG{"));
        assertTrue("Flag should end with }", flag.endsWith("}"));
    }

    @Test
    public void lesson_FlagIsNotTrivial() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        // Flag should be substantial length (not just "FLAG{x}")
        assertTrue("Flag should be at least 15 characters", flag.length() >= 15);
    }

    @Test
    public void lesson_FlagContainsExpectedPattern() throws Exception {
        Field flagField = LessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        // Verify it contains reverse engineering related terms
        String lowerFlag = flag.toLowerCase();
        assertTrue("Flag should reference reverse engineering", 
            lowerFlag.contains("revers") || lowerFlag.contains("r3v3rs"));
    }

    @Test
    public void lesson_FlagIsAccessibleViaReflection() {
        // This test verifies that the flag CAN be found through reverse engineering
        try {
            Field[] fields = LessonFragment.class.getDeclaredFields();
            boolean foundFlag = false;
            
            for (Field field : fields) {
                if (field.getName().contains("FLAG") || field.getName().contains("flag")) {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value instanceof String) {
                        String strValue = (String) value;
                        if (strValue.contains("FLAG{") || strValue.contains("flag{")) {
                            foundFlag = true;
                            break;
                        }
                    }
                }
            }
            
            assertTrue("Flag should be discoverable via reflection", foundFlag);
        } catch (Exception e) {
            fail("Should be able to access flag through reflection: " + e.getMessage());
        }
    }

    @Test
    public void lesson_VerifyExpectedFlagValue() throws Exception {
        // This test documents the expected flag for validation
        Field flagField = LessonFragment.class.getDeclaredField("HIDDEN_FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertEquals("Expected flag value", "FLAG{R3v3rs3_Eng1n33r1ng_M4st3r_2024}", flag);
    }
}
