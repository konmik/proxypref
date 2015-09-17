package proxypref;

import android.content.SharedPreferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Map;

import proxypref.annotation.DefaultString;
import proxypref.annotation.Preference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProxyHandlerTest {

    interface StringTest {
        String test();
        @DefaultString("defValue")
        String getTest();
        void test(String value);
        @Preference("test")
        String getStrictName();
    }

    SharedPreferences pref;
    StringTest test;

    @Before
    public void setUp() throws Exception {
        pref = Mockito.mock(SharedPreferences.class);
        test = ProxyPreferences.build(StringTest.class, pref);
    }

    @After
    public void tearDown() throws Exception {
        ProxyHandler.clearCache();
    }

    @Test
    public void get_returns_value_from_preferences() throws Exception {
        when(pref.getAll())
            .thenAnswer(TestUtil.answerMapValue("test", "value"));
        assertEquals("value", test.test());
    }

    @Test
    public void get_returns_null_when_empty_preference() throws Exception {
        assertNull(test.test());
    }

    @Test
    public void get_returns_default_value_when_empty_preference_and_default_value_is_set() throws Exception {
        assertEquals("defValue", test.getTest());
    }

    @Test
    public void set_puts_data_into_preference() throws Exception {
        SharedPreferences.Editor editor = TestUtil.mockEditor(pref);

        test.test("value");

        InOrder order = inOrder(pref, editor);
        order.verify(pref, times(1)).edit();
        order.verify(editor, times(1)).putString("test", "value");
        order.verify(editor, times(1)).apply();
    }

    @Test
    public void set_null_removes_value() throws Exception {
        SharedPreferences.Editor editor = TestUtil.mockEditor(pref);

        test.test(null);

        InOrder order = inOrder(pref, editor);
        order.verify(pref, times(1)).edit();
        order.verify(editor, times(1)).remove("test");
        order.verify(editor, times(1)).apply();
    }

    @Test
    public void key_is_being_taken_from_Preference_annotation() throws Exception {
        Map map = mock(Map.class);
        when(pref.getAll()).thenReturn(map);
        test.getStrictName();
        verify(map).get("test");
    }
}
