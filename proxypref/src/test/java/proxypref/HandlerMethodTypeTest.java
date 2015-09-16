package proxypref;

import org.junit.Test;

import rx.Observable;
import rx.functions.Action1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HandlerMethodTypeTest {

    interface Types {
        void set(String s);
        String get();
        Observable<String> observable();
        Action1<String> action();
    }

    @Test
    public void testTypes() throws Exception {
        assertEquals(Handler.MethodType.GET, Handler.getMethodType(Types.class.getDeclaredMethod("get")));
        assertEquals(Handler.MethodType.SET, Handler.getMethodType(Types.class.getDeclaredMethod("set", String.class)));
        assertEquals(Handler.MethodType.OBSERVABLE, Handler.getMethodType(Types.class.getDeclaredMethod("observable")));
        assertEquals(Handler.MethodType.ACTION, Handler.getMethodType(Types.class.getDeclaredMethod("action")));
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
        assertFalse(Handler.getMethodType(IsSetTests.class.getDeclaredMethod("test")).isSet);
        assertTrue(Handler.getMethodType(IsSetTests.class.getDeclaredMethod("test", String.class)).isSet);
        assertFalse(Handler.getMethodType(IsSetTests.class.getDeclaredMethod("getTest")).isSet);
        assertFalse(Handler.getMethodType(IsSetTests.class.getDeclaredMethod("testObservable")).isSet);
        assertTrue(Handler.getMethodType(IsSetTests.class.getDeclaredMethod("set")).isSet);
    }
}
