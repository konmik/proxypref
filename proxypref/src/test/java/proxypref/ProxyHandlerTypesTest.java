package proxypref;

import android.content.SharedPreferences;

import org.junit.Test;
import org.mockito.InOrder;

import java.util.HashSet;
import java.util.Set;

import rx.functions.Action2;
import rx.functions.Func1;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static proxypref.TestUtil.answerMapValue;

public class ProxyHandlerTypesTest {

    interface TestTypes {
        String getString();
        void setString(String x);
        Integer getInt();
        void setInt(Integer x);
        Long getLong();
        void setLong(Long x);
        Float getFloat();
        void setFloat(Float x);
        Boolean getBoolean();
        void setBoolean(Boolean x);
        Set<String> set();
        void set(Set<String> x);
    }

    @Test
    public void testTypes() throws Exception {
        testType("string", "setValue", "getValue",
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
        Func1<SharedPreferences.Editor, SharedPreferences.Editor> whenPut,
        Action2<SharedPreferences.Editor, T> verifyPut,
        Func1<TestTypes, T> doGet, Action2<TestTypes, T> doSet) {

        SharedPreferences pref = mock(SharedPreferences.class);
        TestTypes test = ProxyPreferences.build(TestTypes.class, pref);

        when(pref.getAll())
            .thenAnswer(answerMapValue(methodName, getValue));

        assertEquals(getValue, doGet.call(test));

        SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);
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
}
