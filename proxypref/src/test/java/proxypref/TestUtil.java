package proxypref;

import android.content.SharedPreferences;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {
    static <T> Answer<T> answerDefault(final String name) {
        return new Answer<T>() {
            @Override
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                return name.equals(invocationOnMock.getArguments()[0]) ? (T)invocationOnMock.getArguments()[1] : null;
            }
        };
    }

    static Answer<Map> answerMapValue(final String name, final Object value) {
        return new Answer<Map>() {
            @Override
            public Map answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new HashMap<String, Object>() {{
                    put(name, value);
                }};
            }
        };
    }

    static SharedPreferences.Editor mockEditor(SharedPreferences prefMock) {
        SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);

        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putFloat(anyString(), anyFloat())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putStringSet(anyString(), any(Set.class))).thenReturn(editor);

        when(editor.clear()).thenReturn(editor);
        when(editor.remove(anyString())).thenReturn(editor);
        doReturn(true).when(editor).commit();

        when(prefMock.edit()).thenReturn(editor);
        return editor;
    }

    static Map mockMapValue(String key, Object value) {
        Map map = mock(Map.class);
        when(map.get(eq(key))).thenReturn(value);
        return map;
    }
}
