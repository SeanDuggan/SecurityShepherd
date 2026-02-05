package com.owasp.app;

import com.owasp.app.ui.challenges.reverseengineering.ReverseEngineering3Model;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Unit tests for Reverse Engineering Challenge 3
 * Tests obfuscated flag with multiple string parts
 * Uses reflection to test without instantiating the ViewModel
 */
public class ReverseEngineeringChallenge3Test {

    @Test
    public void challenge3_ValidateFlagMethodExists() throws Exception {
        // Verify the validation method exists
        Method validateMethod = ReverseEngineering3Model.class.getDeclaredMethod("validateFlag", String.class);
        assertNotNull("validateFlag method should exist", validateMethod);
    }

    @Test
    public void challenge3_AllFlagPartsExist() throws Exception {
        // Verify all 5 parts exist as separate constants
        Field part1 = ReverseEngineering3Model.class.getDeclaredField("PART_1");
        Field part2 = ReverseEngineering3Model.class.getDeclaredField("PART_2");
        Field part3 = ReverseEngineering3Model.class.getDeclaredField("PART_3");
        Field part4 = ReverseEngineering3Model.class.getDeclaredField("PART_4");
        Field part5 = ReverseEngineering3Model.class.getDeclaredField("PART_5");
        
        assertNotNull("PART_1 should exist", part1);
        assertNotNull("PART_2 should exist", part2);
        assertNotNull("PART_3 should exist", part3);
        assertNotNull("PART_4 should exist", part4);
        assertNotNull("PART_5 should exist", part5);
    }

    @Test
    public void challenge3_CanAccessFlagParts() throws Exception {
        // Access each part via reflection
        Field part1 = ReverseEngineering3Model.class.getDeclaredField("PART_1");
        Field part2 = ReverseEngineering3Model.class.getDeclaredField("PART_2");
        Field part3 = ReverseEngineering3Model.class.getDeclaredField("PART_3");
        Field part4 = ReverseEngineering3Model.class.getDeclaredField("PART_4");
        Field part5 = ReverseEngineering3Model.class.getDeclaredField("PART_5");
        
        part1.setAccessible(true);
        part2.setAccessible(true);
        part3.setAccessible(true);
        part4.setAccessible(true);
        part5.setAccessible(true);
        
        assertEquals("PART_1 should be opening", "OWASP{", part1.get(null));
        assertEquals("PART_2 should be 'Obfuscated'", "Obfuscated", part2.get(null));
        assertEquals("PART_3 should be '_Hard_'", "_Hard_", part3.get(null));
        assertEquals("PART_4 should be 'Challenge'", "Challenge", part4.get(null));
        assertEquals("PART_5 should be closing", "}", part5.get(null));
    }

    @Test
    public void challenge3_FlagPartsAreStatic() throws Exception {
        // Verify all parts are static constants
        for (int i = 1; i <= 5; i++) {
            Field part = ReverseEngineering3Model.class.getDeclaredField("PART_" + i);
            assertTrue("PART_" + i + " should be static", 
                java.lang.reflect.Modifier.isStatic(part.getModifiers()));
            assertTrue("PART_" + i + " should be final", 
                java.lang.reflect.Modifier.isFinal(part.getModifiers()));
        }
    }

    @Test
    public void challenge3_ConstructFlagMethodExists() throws Exception {
        // Verify the private constructFlag method exists
        Method constructFlagMethod = ReverseEngineering3Model.class.getDeclaredMethod("constructFlag");
        assertNotNull("constructFlag method should exist", constructFlagMethod);
        
        assertTrue("constructFlag should be private", 
            java.lang.reflect.Modifier.isPrivate(constructFlagMethod.getModifiers()));
    }

    @Test
    public void challenge3_ConstructFlagMethodIsPrivate() throws Exception {
        // Verify method exists and is private
        Method constructFlagMethod = ReverseEngineering3Model.class.getDeclaredMethod("constructFlag");
        assertTrue("constructFlag should be private", 
            java.lang.reflect.Modifier.isPrivate(constructFlagMethod.getModifiers()));
    }

    @Test
    public void challenge3_ReconstructFlagFromParts() throws Exception {
        // Simulate reverse engineering: find all parts and reconstruct
        Field part1 = ReverseEngineering3Model.class.getDeclaredField("PART_1");
        Field part2 = ReverseEngineering3Model.class.getDeclaredField("PART_2");
        Field part3 = ReverseEngineering3Model.class.getDeclaredField("PART_3");
        Field part4 = ReverseEngineering3Model.class.getDeclaredField("PART_4");
        Field part5 = ReverseEngineering3Model.class.getDeclaredField("PART_5");
        
        part1.setAccessible(true);
        part2.setAccessible(true);
        part3.setAccessible(true);
        part4.setAccessible(true);
        part5.setAccessible(true);
        
        StringBuilder reconstructed = new StringBuilder();
        reconstructed.append((String) part1.get(null));
        reconstructed.append((String) part2.get(null));
        reconstructed.append((String) part3.get(null));
        reconstructed.append((String) part4.get(null));
        reconstructed.append((String) part5.get(null));
        
        String flag = reconstructed.toString();
        
        // Verify reconstructed flag is correct
        assertEquals("Reconstructed flag should match expected", 
            "OWASP{Obfuscated_Hard_Challenge}", flag);
    }

    @Test
    public void challenge3_DiscoverFlagThroughFieldEnumeration() {
        // Complete reverse engineering simulation
        try {
            Field[] fields = ReverseEngineering3Model.class.getDeclaredFields();
            StringBuilder flagBuilder = new StringBuilder();
            
            // Find and sort PART_ fields
            for (int i = 1; i <= 5; i++) {
                for (Field field : fields) {
                    if (field.getName().equals("PART_" + i)) {
                        field.setAccessible(true);
                        String part = (String) field.get(null);
                        flagBuilder.append(part);
                        break;
                    }
                }
            }
            
            String discoveredFlag = flagBuilder.toString();
            assertTrue("Discovered flag should not be empty", discoveredFlag.length() > 0);
            assertEquals("Discovered flag should be correct", "OWASP{Obfuscated_Hard_Challenge}", discoveredFlag);
            
        } catch (Exception e) {
            fail("Should be able to discover flag: " + e.getMessage());
        }
    }

    @Test
    public void challenge3_FirstPartStartsWithOWASP() throws Exception {
        Field part1 = ReverseEngineering3Model.class.getDeclaredField("PART_1");
        part1.setAccessible(true);
        String firstPart = (String) part1.get(null);
        
        assertTrue("First part should start with OWASP{", firstPart.startsWith("OWASP{"));
    }

    @Test
    public void challenge3_XorDecodeMethodExists() throws Exception {
        // Verify XOR decode method exists (even if not currently used)
        Method xorDecodeMethod = ReverseEngineering3Model.class.getDeclaredMethod("xorDecode", String.class);
        assertNotNull("xorDecode method should exist", xorDecodeMethod);
        
        assertTrue("xorDecode should be private", 
            java.lang.reflect.Modifier.isPrivate(xorDecodeMethod.getModifiers()));
    }

    @Test
    public void challenge3_ObfuscationMakesStaticAnalysisHarder() throws Exception {
        // Verify that no single PART field contains the complete flag
        String completeFlag = "OWASP{Obfuscated_Hard_Challenge}";
        
        for (int i = 1; i <= 5; i++) {
            Field field = ReverseEngineering3Model.class.getDeclaredField("PART_" + i);
            field.setAccessible(true);
            String value = (String) field.get(null);
            assertNotEquals("No single PART should contain complete flag", 
                completeFlag, value);
        }
    }

}

