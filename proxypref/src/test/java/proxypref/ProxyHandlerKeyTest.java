package proxypref;

import org.junit.Test;

import java.lang.reflect.Method;

import proxypref.annotation.Preference;

import static org.junit.Assert.assertEquals;

public class ProxyHandlerKeyTest {

    interface NameTests {
        String g();
        String get();
        void setCamelCase(String a);
        String getALLBIG();
        String methodGet2();
        void methodSet2(String a);
        @Preference("customName1")
        String methodCustom1();
        @Preference("customName2")
        void methodCustom2(String a);
    }

    @Test
    public void testName() throws Exception {
        assertEquals("g", getMethodKey("g"));
        assertEquals("get", getMethodKey("get"));
        assertEquals("camelCase", getMethodKey("setCamelCase", String.class));
        assertEquals("aLLBIG", getMethodKey("getALLBIG"));
        assertEquals("methodGet2", getMethodKey("methodGet2"));
        assertEquals("methodSet2", getMethodKey("methodSet2", String.class));
        assertEquals("customName1", getMethodKey("methodCustom1"));
        assertEquals("customName2", getMethodKey("methodCustom2", String.class));
    }

    private String getMethodKey(String name) throws NoSuchMethodException {
        Method method = NameTests.class.getDeclaredMethod(name);
        return ProxyHandler.getPreferenceKey(ProxyHandler.getMethodType(method), method);
    }

    private String getMethodKey(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = NameTests.class.getDeclaredMethod(name, parameterTypes);
        return ProxyHandler.getPreferenceKey(ProxyHandler.getMethodType(method), method);
    }
}
