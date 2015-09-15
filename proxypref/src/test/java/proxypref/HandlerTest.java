package proxypref;

import android.content.SharedPreferences;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HandlerTest {
    interface StringTest {
        String test();
        void test(String value);
        String getTest(String defValue);
        Observable<String> testObservable();
        Observable<String> testObservable(String defValue);
        Action1<String> set();
    }

    @Test
    public void testInvoke() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        StringTest test = ProxyPreferences.build(StringTest.class, pref);

        when(pref.contains("test")).thenReturn(true);
        when(pref.getString(anyString(), anyString()))
            .thenAnswer(answerValue("test", "value"));
        assertEquals("value", test.test());

        when(pref.getString(anyString(), anyString()))
            .thenAnswer(answerValue("test", "value"));
        assertEquals("value", test.getTest("defValue"));

        when(pref.getString(anyString(), anyString()))
            .thenAnswer(answerDefault("test"));
        assertEquals("defValue", test.getTest("defValue"));
        assertEquals(null, test.getTest(null));

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

    @Test
    public void testObservable() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        when(pref.contains("testObservable")).thenReturn(true);
        when(pref.getString(anyString(), anyString()))
            .thenAnswer(answerValue("testObservable", "value"));

        StringTest test = ProxyPreferences.build(StringTest.class, pref);

        final AtomicReference<SharedPreferences.OnSharedPreferenceChangeListener> listener = new AtomicReference<>();
        final AtomicReference<SharedPreferences.OnSharedPreferenceChangeListener> unregisteredListener = new AtomicReference<>();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                listener.set((SharedPreferences.OnSharedPreferenceChangeListener)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(pref).registerOnSharedPreferenceChangeListener(any(SharedPreferences.OnSharedPreferenceChangeListener.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals(invocationOnMock.getArguments()[0], listener.get());
                assertNotNull(invocationOnMock.getArguments()[0]);
                unregisteredListener.set((SharedPreferences.OnSharedPreferenceChangeListener)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(pref).unregisterOnSharedPreferenceChangeListener(any(SharedPreferences.OnSharedPreferenceChangeListener.class));

        TestSubscriber<String> subscriber = new TestSubscriber<>();

        Subscription subscription = test.testObservable().subscribe(subscriber);
        subscriber.assertReceivedOnNext(Collections.singletonList("value"));

        when(pref.getString(anyString(), anyString()))
            .thenAnswer(answerValue("testObservable", "value2"));
        listener.get().onSharedPreferenceChanged(pref, "testObservable");
        subscriber.assertReceivedOnNext(asList("value", "value2"));

        listener.get().onSharedPreferenceChanged(pref, "some other key");
        subscriber.assertReceivedOnNext(asList("value", "value2"));

        subscription.unsubscribe();

        subscriber.assertNoTerminalEvent();
        verify(pref, times(1)).unregisterOnSharedPreferenceChangeListener(listener.get());
    }

    @Test
    public void testObservableDefault() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        when(pref.getString(anyString(), anyString()))
            .thenAnswer(answerDefault("testObservable"));
        StringTest test = ProxyPreferences.build(StringTest.class, pref);

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        test.testObservable("1").subscribe(subscriber);
        subscriber.assertReceivedOnNext(Collections.singletonList("1"));
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
            .thenAnswer(answerValue("set", value));

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

    private <T> Answer<T> answerDefault(final String name) {
        return new Answer<T>() {
            @Override
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                return name.equals(invocationOnMock.getArguments()[0]) ? (T)invocationOnMock.getArguments()[1] : null;
            }
        };
    }

    private <T> Answer<T> answerValue(final String name, final T value) {
        return new Answer<T>() {
            @Override
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                return name.equals(invocationOnMock.getArguments()[0]) ? value : null;
            }
        };
    }

    interface NameTests {
        String g();
        String get();
        void setCamelCase(String a);
        String getALLBIG();
        String get1withDefault(String s);
        String methodGet2();
        void methodSet2(String a);
        @Preference("customName1")
        String methodCustom1();
        @Preference("customName2")
        void methodCustom2(String a);
    }

    @Test
    public void testName() throws Exception {
        assertEquals("g", Handler.getPreferenceName(NameTests.class.getDeclaredMethod("g")));
        assertEquals("get", Handler.getPreferenceName(NameTests.class.getDeclaredMethod("get")));
        assertEquals("camelCase", Handler.getPreferenceName(NameTests.class.getDeclaredMethod("setCamelCase", String.class)));
        assertEquals("aLLBIG", Handler.getPreferenceName(NameTests.class.getDeclaredMethod("getALLBIG")));
        assertEquals("1withDefault", Handler.getPreferenceName(NameTests.class.getDeclaredMethod("get1withDefault", String.class)));
        assertEquals("methodGet2", Handler.getPreferenceName(NameTests.class.getDeclaredMethod("methodGet2")));
        assertEquals("methodSet2", Handler.getPreferenceName(NameTests.class.getDeclaredMethod("methodSet2", String.class)));
        assertEquals("customName1", Handler.getPreferenceName(NameTests.class.getDeclaredMethod("methodCustom1")));
        assertEquals("customName2", Handler.getPreferenceName(NameTests.class.getDeclaredMethod("methodCustom2", String.class)));
    }

    interface IsSetTests {
        String test();
        void test(String value);
        String getTest(String defValue);
        Observable<String> testObservable();
        Action1<String> set();
    }

    @Test
    public void testIsSet() throws Exception {
        assertFalse(Handler.isSet(IsSetTests.class.getDeclaredMethod("test")));
        assertTrue(Handler.isSet(IsSetTests.class.getDeclaredMethod("test", String.class)));
        assertFalse(Handler.isSet(IsSetTests.class.getDeclaredMethod("getTest", String.class)));
        assertFalse(Handler.isSet(IsSetTests.class.getDeclaredMethod("testObservable")));
        assertTrue(Handler.isSet(IsSetTests.class.getDeclaredMethod("set")));
    }

    interface TestTypes {
        String getString();
        void setString(String x);
        int getInt();
        void setInt(int x);
        Long getLong();
        void setLong(Long x);
        float getFloat();
        void setFloat(float x);
        boolean getBoolean();
        void setBoolean(boolean x);
        Set<String> set();
        void set(Set<String> x);
    }

    @Test
    public void testTypes() throws Exception {
        testType("string", "setValue", "getValue",
            new Func1<SharedPreferences, String>() {
                @Override
                public String call(SharedPreferences preferences) {
                    return preferences.getString(eq("string"), anyString());
                }
            },
            new Func1<SharedPreferences.Editor, SharedPreferences.Editor>() {
                @Override
                public SharedPreferences.Editor call(SharedPreferences.Editor editor) {
                    return editor.putString(anyString(), anyString());
                }
            },
            new Action2<SharedPreferences.Editor, String>() {
                @Override
                public void call(SharedPreferences.Editor editor, String value) {
                    editor.putString("string", value);
                }
            },
            new Func1<TestTypes, String>() {
                @Override
                public String call(TestTypes testTypes) {
                    return testTypes.getString();
                }
            },
            new Action2<TestTypes, String>() {
                @Override
                public void call(TestTypes testTypes, String s) {
                    testTypes.setString(s);
                }
            });
        testType("int", 1, 2,
            new Func1<SharedPreferences, Integer>() {
                @Override
                public Integer call(SharedPreferences preferences) {
                    return preferences.getInt(eq("int"), anyInt());
                }
            },
            new Func1<SharedPreferences.Editor, SharedPreferences.Editor>() {
                @Override
                public SharedPreferences.Editor call(SharedPreferences.Editor editor) {
                    return editor.putInt(anyString(), anyInt());
                }
            },
            new Action2<SharedPreferences.Editor, Integer>() {
                @Override
                public void call(SharedPreferences.Editor editor, Integer value) {
                    editor.putInt("int", value);
                }
            },
            new Func1<TestTypes, Integer>() {
                @Override
                public Integer call(TestTypes testTypes) {
                    return testTypes.getInt();
                }
            },
            new Action2<TestTypes, Integer>() {
                @Override
                public void call(TestTypes testTypes, Integer s) {
                    testTypes.setInt(s);
                }
            });
        testType("long", 1l, 2l,
            new Func1<SharedPreferences, Long>() {
                @Override
                public Long call(SharedPreferences preferences) {
                    return preferences.getLong(eq("long"), anyLong());
                }
            },
            new Func1<SharedPreferences.Editor, SharedPreferences.Editor>() {
                @Override
                public SharedPreferences.Editor call(SharedPreferences.Editor editor) {
                    return editor.putLong(anyString(), anyLong());
                }
            },
            new Action2<SharedPreferences.Editor, Long>() {
                @Override
                public void call(SharedPreferences.Editor editor, Long value) {
                    editor.putLong("long", value);
                }
            },
            new Func1<TestTypes, Long>() {
                @Override
                public Long call(TestTypes testTypes) {
                    return testTypes.getLong();
                }
            },
            new Action2<TestTypes, Long>() {
                @Override
                public void call(TestTypes testTypes, Long s) {
                    testTypes.setLong(s);
                }
            });
        testType("float", 1f, 2f,
            new Func1<SharedPreferences, Float>() {
                @Override
                public Float call(SharedPreferences preferences) {
                    return preferences.getFloat(eq("float"), anyFloat());
                }
            },
            new Func1<SharedPreferences.Editor, SharedPreferences.Editor>() {
                @Override
                public SharedPreferences.Editor call(SharedPreferences.Editor editor) {
                    return editor.putFloat(anyString(), anyFloat());
                }
            },
            new Action2<SharedPreferences.Editor, Float>() {
                @Override
                public void call(SharedPreferences.Editor editor, Float value) {
                    editor.putFloat("float", value);
                }
            },
            new Func1<TestTypes, Float>() {
                @Override
                public Float call(TestTypes testTypes) {
                    return testTypes.getFloat();
                }
            },
            new Action2<TestTypes, Float>() {
                @Override
                public void call(TestTypes testTypes, Float s) {
                    testTypes.setFloat(s);
                }
            });
        testType("boolean", true, false,
            new Func1<SharedPreferences, Boolean>() {
                @Override
                public Boolean call(SharedPreferences preferences) {
                    return preferences.getBoolean(eq("boolean"), anyBoolean());
                }
            },
            new Func1<SharedPreferences.Editor, SharedPreferences.Editor>() {
                @Override
                public SharedPreferences.Editor call(SharedPreferences.Editor editor) {
                    return editor.putBoolean(anyString(), anyBoolean());
                }
            },
            new Action2<SharedPreferences.Editor, Boolean>() {
                @Override
                public void call(SharedPreferences.Editor editor, Boolean value) {
                    editor.putBoolean("boolean", value);
                }
            },
            new Func1<TestTypes, Boolean>() {
                @Override
                public Boolean call(TestTypes testTypes) {
                    return testTypes.getBoolean();
                }
            },
            new Action2<TestTypes, Boolean>() {
                @Override
                public void call(TestTypes testTypes, Boolean s) {
                    testTypes.setBoolean(s);
                }
            });
        testType("set", new HashSet<String>(), new HashSet<String>(),
            new Func1<SharedPreferences, Set<String>>() {
                @Override
                public Set<String> call(SharedPreferences preferences) {
                    return preferences.getStringSet(eq("set"), any(Set.class));
                }
            },
            new Func1<SharedPreferences.Editor, SharedPreferences.Editor>() {
                @Override
                public SharedPreferences.Editor call(SharedPreferences.Editor editor) {
                    return editor.putStringSet(anyString(), any(Set.class));
                }
            },
            new Action2<SharedPreferences.Editor, Set<String>>() {
                @Override
                public void call(SharedPreferences.Editor editor, Set<String> value) {
                    editor.putStringSet("set", value);
                }
            },
            new Func1<TestTypes, Set<String>>() {
                @Override
                public Set<String> call(TestTypes testTypes) {
                    return testTypes.set();
                }
            },
            new Action2<TestTypes, Set<String>>() {
                @Override
                public void call(TestTypes testTypes, Set<String> s) {
                    testTypes.set(s);
                }
            });
    }

    private <T> void testType(String methodName, T getValue, T setValue,
        Func1<SharedPreferences, T> whenGet,
        Func1<SharedPreferences.Editor, SharedPreferences.Editor> whenPut,
        Action2<SharedPreferences.Editor, T> verifyPut,
        Func1<TestTypes, T> doGet, Action2<TestTypes, T> doSet) {

        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        TestTypes test = ProxyPreferences.build(TestTypes.class, pref);

        when(pref.contains(methodName)).thenReturn(true);
        when(whenGet.call(pref))
            .thenAnswer(answerValue(methodName, getValue));

        assertEquals(getValue, doGet.call(test));

        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(pref.edit())
            .thenReturn(editor);
        when(whenPut.call(editor))
            .thenReturn(editor);

        doSet.call(test, setValue);

        InOrder order = inOrder(pref, editor);
        order.verify(pref, times(1)).edit();
        verifyPut.call(order.verify(editor, times(1)), setValue);
        order.verify(editor, times(1)).apply();
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
