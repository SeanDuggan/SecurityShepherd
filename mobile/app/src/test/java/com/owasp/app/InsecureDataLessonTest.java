package com.owasp.app;

import com.owasp.app.ui.lessons.insecuredata.InsecureDataLessonFragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for InsecureDataLessonFragment
 * This lesson doesn't have exposed constants but tests verify the class structure exists
 */
public class InsecureDataLessonTest {

    @Test
    public void testClassExists() {
        assertNotNull("InsecureDataLessonFragment class should exist", 
                     InsecureDataLessonFragment.class);
    }

    @Test
    public void testCreateDatabaseMethodExists() throws Exception {
        Method method = InsecureDataLessonFragment.class.getDeclaredMethod("createDatabase");
        assertNotNull("createDatabase method should exist", method);
    }

    @Test
    public void testInsertKeyMethodExists() throws Exception {
        Method method = InsecureDataLessonFragment.class.getDeclaredMethod("insertKey");
        assertNotNull("insertKey method should exist", method);
    }

    @Test
    public void testShowDetailedInfoMethodExists() throws Exception {
        Method method = InsecureDataLessonFragment.class.getDeclaredMethod("showDetailedInfo");
        assertNotNull("showDetailedInfo method should exist", method);
    }
    
    @Test
    public void testFragmentHasOnCreateViewMethod() throws Exception {
        Method method = InsecureDataLessonFragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testFragmentHasOnDestroyViewMethod() throws Exception {
        Method method = InsecureDataLessonFragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }
}
