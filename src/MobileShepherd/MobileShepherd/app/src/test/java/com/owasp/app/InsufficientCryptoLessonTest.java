package com.owasp.app;

import com.owasp.app.ui.lessons.crypto.InsufficientCryptoLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Unit tests for InsufficientCryptoLessonFragment - testing weak crypto constants and flag parts
 */
public class InsufficientCryptoLessonTest {

    @Test
    public void testWeakAlgorithmConstant() throws Exception {
        Field algoField = InsufficientCryptoLessonFragment.class.getDeclaredField("WEAK_ALGORITHM");
        algoField.setAccessible(true);
        String algorithm = (String) algoField.get(null);
        
        assertEquals("Weak algorithm should be DES", "DES", algorithm);
    }

    @Test
    public void testHardcodedKeyExists() throws Exception {
        Field keyField = InsufficientCryptoLessonFragment.class.getDeclaredField("HARDCODED_KEY");
        keyField.setAccessible(true);
        String key = (String) keyField.get(null);
        
        assertNotNull("Hardcoded key should exist", key);
        assertEquals("DES key should be 8 bytes", 8, key.length());
        assertEquals("Key should match expected value", "BadKey01", key);
    }

    @Test
    public void testFlagPart1() throws Exception {
        Field part1Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P1");
        part1Field.setAccessible(true);
        String part1 = (String) part1Field.get(null);
        
        assertEquals("FLAG_P1 should be 'OWASP{'", "OWASP{", part1);
    }

    @Test
    public void testFlagPart2() throws Exception {
        Field part2Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P2");
        part2Field.setAccessible(true);
        String part2 = (String) part2Field.get(null);
        
        assertEquals("FLAG_P2 should be 'W3ak_'", "W3ak_", part2);
    }

    @Test
    public void testFlagPart3() throws Exception {
        Field part3Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P3");
        part3Field.setAccessible(true);
        String part3 = (String) part3Field.get(null);
        
        assertEquals("FLAG_P3 should be 'DES_'", "DES_", part3);
    }

    @Test
    public void testFlagPart4() throws Exception {
        Field part4Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P4");
        part4Field.setAccessible(true);
        String part4 = (String) part4Field.get(null);
        
        assertEquals("FLAG_P4 should be 'Encryp'", "Encryp", part4);
    }

    @Test
    public void testFlagPart5() throws Exception {
        Field part5Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P5");
        part5Field.setAccessible(true);
        String part5 = (String) part5Field.get(null);
        
        assertEquals("FLAG_P5 should be 't10n}'", "t10n}", part5);
    }

    @Test
    public void testReconstructedFlag() throws Exception {
        Field part1Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P1");
        Field part2Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P2");
        Field part3Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P3");
        Field part4Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P4");
        Field part5Field = InsufficientCryptoLessonFragment.class.getDeclaredField("FLAG_P5");
        
        part1Field.setAccessible(true);
        part2Field.setAccessible(true);
        part3Field.setAccessible(true);
        part4Field.setAccessible(true);
        part5Field.setAccessible(true);
        
        String fullFlag = (String) part1Field.get(null) +
                         (String) part2Field.get(null) +
                         (String) part3Field.get(null) +
                         (String) part4Field.get(null) +
                         (String) part5Field.get(null);
        
        assertEquals("Reconstructed flag should be correct", "OWASP{W3ak_DES_Encrypt10n}", fullFlag);
    }
}
