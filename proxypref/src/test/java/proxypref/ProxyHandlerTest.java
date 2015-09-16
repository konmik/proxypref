package proxypref;

import android.content.SharedPreferences;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import proxypref.annotation.DefaultString;
import rx.functions.Action1;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class ProxyHandlerTest {
    interface StringTest {
        String test();
        void test(String value);
        @DefaultString("defValue")
        String getTest();
        Action1<String> set();
    }

    @Test
    public void testInvoke() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        StringTest test = ProxyPreferences.build(StringTest.class, pref);

        when(pref.contains("test")).thenReturn(true);
        when(pref.getString(anyString(), anyString()))
            .thenAnswer(TestUtil.answerValue("test", "value"));
        assertEquals("value", test.test());

        when(pref.getString(anyString(), anyString()))
            .thenAnswer(TestUtil.answerValue("test", "value"));
        assertEquals("value", test.getTest());

        when(pref.getString(anyString(), anyString()))
            .thenAnswer(TestUtil.answerDefault("test"));
        assertEquals("defValue", test.getTest());

        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(pref.edit())
            .thenReturn(editor);
        when(editor.putString(anyString(), anyString()))
            .thenReturn(editor);

        test.test("value");

        InOrder order = inOrder(pref, editor);
        order.verify(pref, times(1)).edit();
        order.verify(editor, times(1)).putString("test", "value");
        order.verify(editor, times(1)).apply();

        test = ProxyPreferences.build(StringTest.class, pref);
        test.set().call("value");

        order = inOrder(pref, editor);
        order.verify(pref, times(1)).edit();
        order.verify(editor, times(1)).putString("test", "value");
        order.verify(editor, times(1)).apply();

        test = ProxyPreferences.build(StringTest.class, pref);
        test.test("1");

        order = inOrder(pref, editor);
        order.verify(pref, times(1)).edit();
        order.verify(editor, times(1)).putString("test", "1");
        order.verify(editor, times(1)).apply();

        when(editor.remove("test")).thenReturn(editor);

        test = ProxyPreferences.build(StringTest.class, pref);
        test.test(null);

        order = inOrder(pref, editor);
        order.verify(pref, times(1)).edit();
        order.verify(editor, times(1)).remove("test");
        order.verify(editor, times(1)).apply();
    }

    interface SetTest {
        Set<String> getSet();
        void setSet(Set<String> x);
        Set failGetNotParametrized();
        Set<Object> failGetInvalidType();
        void failSetNotParametrized(Set x);
        void failSetInvalidType(Set<Object> x);
    }

    @Test
    public void testSetGet() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        SetTest test = ProxyPreferences.build(SetTest.class, pref);

        HashSet<String> value = new HashSet<>();
        when(pref.contains("set")).thenReturn(true);
        when(pref.getStringSet(anyString(), any(Set.class)))
            .thenAnswer(TestUtil.answerValue("set", value));

        assertEquals(value, test.getSet());
    }

    @Test
    public void testSetSet() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        SetTest test = ProxyPreferences.build(SetTest.class, pref);
        HashSet<String> value = new HashSet<>();

        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(pref.edit())
            .thenReturn(editor);
        when(editor.putStringSet(anyString(), any(Set.class)))
            .thenReturn(editor);

        test.setSet(value);

        InOrder order = inOrder(pref, editor);
        order.verify(pref, times(1)).edit();
        order.verify(editor, times(1)).putStringSet("set", value);
        order.verify(editor, times(1)).apply();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFailGetNotParametrized() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        SetTest test = ProxyPreferences.build(SetTest.class, pref);
        test.failGetNotParametrized();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFailGetInvalidType() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        SetTest test = ProxyPreferences.build(SetTest.class, pref);
        test.failGetInvalidType();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFailSetNotParametrized() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        SetTest test = ProxyPreferences.build(SetTest.class, pref);
        test.failSetNotParametrized(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFailSetInvalidType() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        SetTest test = ProxyPreferences.build(SetTest.class, pref);
        test.failSetInvalidType(null);
    }

    interface WrongType {
        Object getA();
        void set(SharedPreferences x);
        void nothing();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongSetType() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        WrongType test = ProxyPreferences.build(WrongType.class, pref);
        test.set(pref);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongGetType() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        WrongType test = ProxyPreferences.build(WrongType.class, pref);
        when(pref.contains("a")).thenReturn(true);
        test.getA();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongNothing() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        WrongType test = ProxyPreferences.build(WrongType.class, pref);
        test.nothing();
    }
}
