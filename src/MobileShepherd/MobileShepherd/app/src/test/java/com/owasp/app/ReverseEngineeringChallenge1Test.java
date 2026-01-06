package com.owasp.app;

import com.owasp.app.ui.challenges.reverseengineering.Challenge_1_Model;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Unit tests for Reverse Engineering Challenge 1
 * Tests the simplest challenge with a plaintext flag constant
 * Uses reflection to test without instantiating the ViewModel
 */
public class ReverseEngineeringChallenge1Test {

    @Test
    public void challenge1_FlagIsAccessibleViaReflection() throws Exception {
        // Simulate reverse engineering via reflection
        Field flagField = Challenge_1_Model.class.getDeclaredField("FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertNotNull("Flag should exist", flag);
        assertEquals("Should find correct flag", "OWASP{Simple_Flag_Easy_To_Find}", flag);
    }

    @Test
    public void challenge1_ValidateFlagMethodExists() throws Exception {
        // Verify the validateFlag method exists and can be called
        Method validateMethod = Challenge_1_Model.class.getDeclaredMethod("validateFlag", String.class);
        assertNotNull("validateFlag method should exist", validateMethod);
    }

    @Test
    public void challenge1_FlagHasCorrectFormat() throws Exception {
        Field flagField = Challenge_1_Model.class.getDeclaredField("FLAG");
        flagField.setAccessible(true);
        String flag = (String) flagField.get(null);
        
        assertTrue("Flag should start with OWASP{", flag.startsWith("OWASP{"));
        assertTrue("Flag should end with }", flag.endsWith("}"));
    }

    @Test
    public void challenge1_FlagIsStatic() throws Exception {
        // Verify the flag is a static constant (common reverse engineering target)
        Field flagField = Challenge_1_Model.class.getDeclaredField("FLAG");
        
        assertTrue("Flag should be static", 
            java.lang.reflect.Modifier.isStatic(flagField.getModifiers()));
        assertTrue("Flag should be final", 
            java.lang.reflect.Modifier.isFinal(flagField.getModifiers()));
    }

    @Test
    public void challenge1_DiscoverFlagThroughFieldEnumeration() {
        // Simulate how a reverse engineer would find the flag
        try {
            Field[] fields = Challenge_1_Model.class.getDeclaredFields();
            String discoveredFlag = null;
            
            for (Field field : fields) {
                if (field.getName().equals("FLAG") || field.getName().contains("flag")) {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value instanceof String && ((String) value).startsWith("OWASP{")) {
                        discoveredFlag = (String) value;
                        break;
                    }
                }
            }
            
            assertNotNull("Should discover flag through field enumeration", discoveredFlag);
            // Validate the discovered flag matches expected value
            assertEquals("Discovered flag should be correct", "OWASP{Simple_Flag_Easy_To_Find}", discoveredFlag);
        } catch (Exception e) {
            fail("Should be able to discover flag: " + e.getMessage());
        }
    }
}
