package com.owasp.app;

import com.owasp.app.ui.challenges.reverseengineering.ReverseEngineering2Model;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;

import static org.junit.Assert.*;

/**
 * Unit tests for Reverse Engineering Challenge 2
 * Tests Base64 encoded flag challenge
 */
public class ReverseEngineering2Test {

    @Test
    public void challenge2_EncodedFlagExists() throws Exception {
        Field flagField = ReverseEngineering2Model.class.getDeclaredField("ENCODED_FLAG");
        flagField.setAccessible(true);
        String encodedFlag = (String) flagField.get(null);
        
        assertNotNull("Encoded flag should exist", encodedFlag);
        assertTrue("Encoded flag should not be empty", encodedFlag.length() > 0);
    }

    @Test
    public void challenge2_ValidateFlagMethodExists() throws Exception {
        Method validateMethod = ReverseEngineering2Model.class.getDeclaredMethod("validateFlag", String.class);
        assertNotNull("validateFlag method should exist", validateMethod);
    }

    @Test
    public void challenge2_DecodeFlagMethodExists() throws Exception {
        Method decodeMethod = ReverseEngineering2Model.class.getDeclaredMethod("decodeFlag");
        assertNotNull("decodeFlag method should exist", decodeMethod);
        assertTrue("decodeFlag should be private", 
            java.lang.reflect.Modifier.isPrivate(decodeMethod.getModifiers()));
    }

    @Test
    public void challenge2_EncodedFlagIsBase64() throws Exception {
        Field flagField = ReverseEngineering2Model.class.getDeclaredField("ENCODED_FLAG");
        flagField.setAccessible(true);
        String encodedFlag = (String) flagField.get(null);
        
        // Base64 strings should only contain alphanumeric, +, /, and =
        assertTrue("Should be valid Base64", encodedFlag.matches("^[A-Za-z0-9+/]*={0,2}$"));
    }

    @Test
    public void challenge2_FlagIsStatic() throws Exception {
        Field flagField = ReverseEngineering2Model.class.getDeclaredField("ENCODED_FLAG");
        assertTrue("ENCODED_FLAG should be static", 
            java.lang.reflect.Modifier.isStatic(flagField.getModifiers()));
        assertTrue("ENCODED_FLAG should be final", 
            java.lang.reflect.Modifier.isFinal(flagField.getModifiers()));
    }

    @Test
    public void challenge2_DecodedFlagHasCorrectFormat() throws Exception {
        // Get encoded flag via reflection
        Field flagField = ReverseEngineering2Model.class.getDeclaredField("ENCODED_FLAG");
        flagField.setAccessible(true);
        String encodedFlag = (String) flagField.get(null);
        
        // Decode using java.util.Base64 (not Android's Base64)
        byte[] decodedBytes = Base64.getDecoder().decode(encodedFlag);
        String decodedFlag = new String(decodedBytes);
        
        assertNotNull("Decoded flag should not be null", decodedFlag);
        assertTrue("Decoded flag should start with OWASP{", decodedFlag.startsWith("OWASP{"));
        assertTrue("Decoded flag should end with }", decodedFlag.endsWith("}"));
    }
}
