package proxypref;

import android.content.SharedPreferences;

import java.lang.reflect.Proxy;

public class ProxyPreferences {
    public static <T> T build(Class<T> tClass, SharedPreferences pref) {
        //noinspection unchecked
        return (T)Proxy.newProxyInstance(tClass.getClassLoader(), new Class<?>[]{tClass}, new ProxyHandler(pref));
    }
}
