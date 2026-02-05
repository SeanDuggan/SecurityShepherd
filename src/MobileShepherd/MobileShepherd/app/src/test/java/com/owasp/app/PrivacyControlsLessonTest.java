package com.owasp.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Unit tests for Privacy Controls Lesson (M6: Inadequate Privacy Controls)
 * Tests metadata handling and EXIF data functionality
 */
@RunWith(JUnit4.class)
public class PrivacyControlsLessonTest {

    private static final String FLAG_FORMAT_PREFIX = "OWASP{";
    private static final String FLAG_FORMAT_SUFFIX = "}";

    @Test
    public void testFragmentClassExists() throws ClassNotFoundException {
        // Verify the fragment class exists
        Class<?> fragmentClass = Class.forName("com.owasp.app.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        assertNotNull("PrivacyControlsLessonFragment class should exist", fragmentClass);
    }

    @Test
    public void testFragmentExtendsFragment() throws ClassNotFoundException {
        Class<?> fragmentClass = Class.forName("com.owasp.app.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        // Check if it extends Fragment
        boolean extendsFragment = false;
        Class<?> superClass = fragmentClass.getSuperclass();
        while (superClass != null) {
            if (superClass.getSimpleName().equals("Fragment")) {
                extendsFragment = true;
                break;
            }
            superClass = superClass.getSuperclass();
        }
        
        assertTrue("PrivacyControlsLessonFragment should extend Fragment", extendsFragment);
    }

    @Test
    public void testFlagConstantExists() throws Exception {
        Class<?> fragmentClass = Class.forName("com.owasp.app.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        // Use reflection to access the private FLAG field
        Field flagField = fragmentClass.getDeclaredField("FLAG");
        flagField.setAccessible(true);
        String flagValue = (String) flagField.get(null);
        
        assertNotNull("FLAG constant should not be null", flagValue);
        assertFalse("FLAG should not be empty", flagValue.isEmpty());
    }

    @Test
    public void testFlagFormat() throws Exception {
        Class<?> fragmentClass = Class.forName("com.owasp.app.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        Field flagField = fragmentClass.getDeclaredField("FLAG");
        flagField.setAccessible(true);
        String flagValue = (String) flagField.get(null);
        
        assertTrue("FLAG should start with 'OWASP{'", flagValue.startsWith(FLAG_FORMAT_PREFIX));
        assertTrue("FLAG should end with '}'", flagValue.endsWith(FLAG_FORMAT_SUFFIX));
        assertTrue("FLAG should contain meaningful content", flagValue.length() > 7);
    }

    @Test
    public void testPreloadedImageConstantExists() throws Exception {
        Class<?> fragmentClass = Class.forName("com.owasp.app.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        Field preloadedImageField = fragmentClass.getDeclaredField("PRELOADED_IMAGE");
        preloadedImageField.setAccessible(true);
        String preloadedImageValue = (String) preloadedImageField.get(null);
        
        assertNotNull("PRELOADED_IMAGE constant should not be null", preloadedImageValue);
        assertTrue("PRELOADED_IMAGE should be a valid filename", preloadedImageValue.endsWith(".jpg") || preloadedImageValue.endsWith(".jpeg"));
    }

    @Test
    public void testFlagIsRelatedToMetadata() throws Exception {
        Class<?> fragmentClass = Class.forName("com.owasp.app.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        Field flagField = fragmentClass.getDeclaredField("FLAG");
        flagField.setAccessible(true);
        String flagValue = (String) flagField.get(null);
        
        // Flag should contain hints about metadata/EXIF/location
        String lowerFlag = flagValue.toLowerCase();
        boolean hasRelevantKeyword = 
            lowerFlag.contains("metadata") || 
            lowerFlag.contains("exif") || 
            lowerFlag.contains("location") ||
            lowerFlag.contains("leak") ||
            lowerFlag.contains("privacy") ||
            lowerFlag.contains("3x1f") || // leetspeak for EXIF
            lowerFlag.contains("m3t4d4t4") || // leetspeak for metadata
            lowerFlag.contains("l0c4t10n") || // leetspeak for location
            lowerFlag.contains("gps");
        
        assertTrue("FLAG should contain privacy/metadata-related keywords", hasRelevantKeyword);
    }

    @Test
    public void testShowDetailedInfoMethodExists() throws Exception {
        Class<?> fragmentClass = Class.forName("com.owasp.app.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        try {
            fragmentClass.getDeclaredMethod("showDetailedInfo");
        } catch (NoSuchMethodException e) {
            fail("showDetailedInfo() method should exist for FAB functionality");
        }
    }

    @Test
    public void testRequiredMethodsExist() throws Exception {
        Class<?> fragmentClass = Class.forName("com.owasp.app.ui.lessons.privacycontrols.PrivacyControlsLessonFragment");
        
        // Check for key methods
        assertNotNull("loadPreloadedImage method should exist", 
            fragmentClass.getDeclaredMethod("loadPreloadedImage"));
        assertNotNull("analyzeCurrentImage method should exist", 
            fragmentClass.getDeclaredMethod("analyzeCurrentImage"));
        assertNotNull("createPreloadedImageWithFlag method should exist", 
            fragmentClass.getDeclaredMethod("createPreloadedImageWithFlag", File.class));
    }
}
