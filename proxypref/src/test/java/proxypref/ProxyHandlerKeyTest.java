package proxypref;

import android.content.SharedPreferences;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import proxypref.annotation.Preference;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProxyHandlerKeyTest {

    interface NameTests {
        String g();
        String get();
        String getALLBIG();
        String methodGet2();
        @Preference("customName1")
        String methodCustom1();

        void setCamelCase(String a);
        void methodSet2(String a);
        @Preference("customName2")
        void methodCustom2(String a);
    }

    @Test
    public void testName() throws Throwable {
        verifyGetMethodToKey("g", "g");
        verifyGetMethodToKey("get", "get");
        verifyGetMethodToKey("getALLBIG", "aLLBIG");
        verifyGetMethodToKey("methodGet2", "methodGet2");
        verifyGetMethodToKey("methodCustom1", "customName1");

        verifyPutMethodToKey("setCamelCase", "camelCase");
        verifyPutMethodToKey("methodSet2", "methodSet2");
        verifyPutMethodToKey("methodCustom2", "customName2");
    }

    private void verifyGetMethodToKey(String methodName, String expectedKey) throws Throwable {
        SharedPreferences pref = mock(SharedPreferences.class);
        Map map = mock(Map.class);
        when(pref.getAll()).thenReturn(map);
        Method method = NameTests.class.getDeclaredMethod(methodName);
        new ProxyHandler(pref, false).invoke(NameTests.class, method, new Object[0]);
        verify(map).get(expectedKey);
    }

    private void verifyPutMethodToKey(String methodName, String expectedKey) throws Throwable {
        SharedPreferences pref = mock(SharedPreferences.class);
        Method method = NameTests.class.getDeclaredMethod(methodName, String.class);

        SharedPreferences.Editor editor = TestUtil.mockEditor(pref);

        new ProxyHandler(pref, false).invoke(NameTests.class, method, new Object[]{""});

        verify(editor, times(1)).putString(eq(expectedKey), anyString());
    }
}
