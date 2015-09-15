package proxypref.test;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

import proxypref.ProxyPreferences;

public class TestActivityTest extends ActivityInstrumentationTestCase2<TestActivity> {

    interface Preferences {
        String getTestString();
        void testString(String x);

        Integer testInteger(Integer def);
        void settestInteger(int x);
    }

    public TestActivityTest() {
        super(TestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testPreferences() throws Exception {
        SharedPreferences preferences = getActivity().getSharedPreferences("1", 0);
        preferences.edit().clear().apply();
        Preferences pref = ProxyPreferences.build(Preferences.class, preferences);

        assertNull(pref.getTestString());
        pref.testString("123");
        assertEquals("123", pref.getTestString());
        assertEquals("123", preferences.getString("testString", null));

        assertEquals(null, pref.testInteger(null));
        pref.settestInteger(123);
        assertEquals((Integer)123, pref.testInteger(1));
        assertEquals(123, preferences.getInt("testInteger", 1));
    }
}