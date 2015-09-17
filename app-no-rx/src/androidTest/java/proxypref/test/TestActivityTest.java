package proxypref.test;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import java.util.Set;

import proxypref.ProxyPreferences;
import proxypref.annotation.DefaultSet;
import proxypref.annotation.DefaultString;
import proxypref.annotation.Preference;

public class TestActivityTest extends ActivityInstrumentationTestCase2<TestActivity> {

    interface MyPreferences {

        // access with get/set prefix
        String getTestString(); // key = testString
        void setTestString(String x); // key = testString

        // without get/set prefix
        Integer testInteger(); // key = testInteger
        void testInteger(Integer x); // key = testInteger

        // ProGuard ready
        @Preference("username")
        String a12();               // key = username

        // Default value
        @DefaultString("user256")
        String username();          // key = username

        // Default set
        @DefaultSet({"1", "2", "3"})
        Set<String> getSomeSet();   // key = someSet
    }

    public TestActivityTest() {
        super(TestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    @UiThreadTest
    public void testPreferences() throws Exception {
        SharedPreferences shared = getActivity().getSharedPreferences("1", 0);
        shared.edit().clear().apply();
        MyPreferences pref = ProxyPreferences.build(MyPreferences.class, shared);

        assertNull(pref.getTestString());
        pref.setTestString("123");
        assertEquals("123", pref.getTestString());
        assertEquals("123", shared.getString("testString", null));

        assertEquals(null, pref.testInteger());
        pref.testInteger(123);
        assertEquals((Integer)123, pref.testInteger());
        assertEquals(123, shared.getInt("testInteger", 1));
    }
}