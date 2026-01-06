package com.owasp.app;

import com.owasp.app.ui.lessons.insecurecomm.InsecureCommLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Unit tests for InsecureCommLessonFragment - testing obfuscated flag parts
 */
public class InsecureCommLessonTest {

    @Test
    public void testFlagPart1() throws Exception {
        Field part1Field = InsecureCommLessonFragment.class.getDeclaredField("PART1");
        part1Field.setAccessible(true);
        String part1 = (String) part1Field.get(null);
        
        assertEquals("PART1 should be 'OWASP'", "OWASP", part1);
    }

    @Test
    public void testFlagPart2() throws Exception {
        Field part2Field = InsecureCommLessonFragment.class.getDeclaredField("PART2");
        part2Field.setAccessible(true);
        String part2 = (String) part2Field.get(null);
        
        assertEquals("PART2 should be '{H1TTP'", "{H1TTP", part2);
    }

    @Test
    public void testFlagPart3() throws Exception {
        Field part3Field = InsecureCommLessonFragment.class.getDeclaredField("PART3");
        part3Field.setAccessible(true);
        String part3 = (String) part3Field.get(null);
        
        assertEquals("PART3 should be '_Insec'", "_Insec", part3);
    }

    @Test
    public void testFlagPart4() throws Exception {
        Field part4Field = InsecureCommLessonFragment.class.getDeclaredField("PART4");
        part4Field.setAccessible(true);
        String part4 = (String) part4Field.get(null);
        
        assertEquals("PART4 should be 'ure_F1'", "ure_F1", part4);
    }

    @Test
    public void testFlagPart5() throws Exception {
        Field part5Field = InsecureCommLessonFragment.class.getDeclaredField("PART5");
        part5Field.setAccessible(true);
        String part5 = (String) part5Field.get(null);
        
        assertEquals("PART5 should be 'nd}'", "nd}", part5);
    }

    @Test
    public void testReconstructedFlag() throws Exception {
        Field part1Field = InsecureCommLessonFragment.class.getDeclaredField("PART1");
        Field part2Field = InsecureCommLessonFragment.class.getDeclaredField("PART2");
        Field part3Field = InsecureCommLessonFragment.class.getDeclaredField("PART3");
        Field part4Field = InsecureCommLessonFragment.class.getDeclaredField("PART4");
        Field part5Field = InsecureCommLessonFragment.class.getDeclaredField("PART5");
        
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
        
        assertEquals("Reconstructed flag should be correct", "OWASP{H1TTP_Insecure_F1nd}", fullFlag);
    }

    @Test
    public void testAllPartsExist() throws Exception {
        String[] partNames = {"PART1", "PART2", "PART3", "PART4", "PART5"};
        
        for (String partName : partNames) {
            Field partField = InsecureCommLessonFragment.class.getDeclaredField(partName);
            partField.setAccessible(true);
            Object partValue = partField.get(null);
            
            assertNotNull(partName + " should exist", partValue);
            assertTrue(partName + " should be a String", partValue instanceof String);
            assertFalse(partName + " should not be empty", ((String) partValue).isEmpty());
        }
    }
}
