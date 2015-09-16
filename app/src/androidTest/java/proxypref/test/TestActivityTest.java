package proxypref.test;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import java.util.concurrent.atomic.AtomicReference;

import proxypref.ProxyPreferences;
import proxypref.annotation.DefaultSet;
import proxypref.annotation.DefaultString;
import rx.Observable;
import rx.functions.Action1;

public class TestActivityTest extends ActivityInstrumentationTestCase2<TestActivity> {

    interface MyPreferences {

        // access with get/set prefix
        String getTestString(); // key = testString
        void setTestString(String x); // key = testString

        // without get/set prefix
        Integer testInteger(); // key = testInteger
        void testInteger(Integer x); // key = testInteger

        // observe with rx.Observable
        Observable<Integer> lastSelectedItem(); // key = lastSelectedItem

        // set with rx.functions.Action1
        Action1<Integer> setLastSelectedItem(); // key = lastSelectedItem

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

        final AtomicReference<Integer> selected = new AtomicReference<>();
        assertEquals(null, selected.get());
        pref.lastSelectedItem().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                selected.set(integer);
            }
        });
        pref.setLastSelectedItem().call(123);
        assertEquals(123, (int)selected.get());
    }
}