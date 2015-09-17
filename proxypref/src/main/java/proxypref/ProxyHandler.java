package proxypref;

import android.content.SharedPreferences;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

import proxypref.method.MethodInfo;

class ProxyHandler implements InvocationHandler {

    private final SharedPreferences pref;
    private final boolean rx;

    public ProxyHandler(SharedPreferences pref, boolean rx) {
        this.pref = pref;
        this.rx = rx;
    }

    private static HashMap<Method, MethodInfo> methods = new HashMap<>();

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable {
        MethodInfo methodInfo = methods.get(method);
        if (methodInfo == null)
            methods.put(method, methodInfo = new MethodInfo(method, rx));
        return methodInfo.invoke(pref, args);
    }

    public static void clearCache() {
        methods.clear();
    }
}
