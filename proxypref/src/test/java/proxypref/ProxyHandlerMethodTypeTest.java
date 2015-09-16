package proxypref;

import org.junit.Test;

import proxypref.method.MethodType;
import rx.Observable;
import rx.functions.Action1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProxyHandlerMethodTypeTest {

    interface Types {
        void set(String s);
        String get();
        Observable<String> observable();
        Action1<String> action();
    }

    @Test
    public void testTypes() throws Exception {
        assertEquals(MethodType.GET, ProxyHandler.getMethodType(Types.class.getDeclaredMethod("get")));
        assertEquals(MethodType.SET, ProxyHandler.getMethodType(Types.class.getDeclaredMethod("set", String.class)));
        assertEquals(MethodType.OBSERVABLE, ProxyHandler.getMethodType(Types.class.getDeclaredMethod("observable")));
        assertEquals(MethodType.ACTION, ProxyHandler.getMethodType(Types.class.getDeclaredMethod("action")));
    }

    interface IsSetTests {
        String test();
        void test(String value);
        String getTest();
        Observable<String> testObservable();
        Action1<String> set();
    }

    @Test
    public void testIsSet() throws Exception {
        assertFalse(ProxyHandler.getMethodType(IsSetTests.class.getDeclaredMethod("test")).isSet);
        assertTrue(ProxyHandler.getMethodType(IsSetTests.class.getDeclaredMethod("test", String.class)).isSet);
        assertFalse(ProxyHandler.getMethodType(IsSetTests.class.getDeclaredMethod("getTest")).isSet);
        assertFalse(ProxyHandler.getMethodType(IsSetTests.class.getDeclaredMethod("testObservable")).isSet);
        assertTrue(ProxyHandler.getMethodType(IsSetTests.class.getDeclaredMethod("set")).isSet);
    }
}
