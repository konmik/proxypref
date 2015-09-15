package proxypref;

import android.content.SharedPreferences;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;

class Handler implements InvocationHandler {

    private final SharedPreferences pref;

    public Handler(SharedPreferences pref) {
        this.pref = pref;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        final Class<?> returnType = method.getReturnType();
        final Type genericReturnType = method.getGenericReturnType();
        final String name = getPreferenceName(method);

        if (isSet(method)) {
            if (returnType.equals(Action1.class)) {
                final Class cls = Util.getRawType(Util.getSingleParameterUpperBound((ParameterizedType)genericReturnType));
                return new Action1() {
                    @Override
                    public void call(Object o) {
                        set(name, cls, o);
                    }
                };
            }

            if (method.getParameterTypes().length < 1)
                throwMethod(method, "Should provide arguments");

            Class<?> aClass = method.getParameterTypes()[0];
            if (aClass.equals(Set.class)) {
                Type type = method.getGenericParameterTypes()[0];
                if (!(type instanceof ParameterizedType))
                    throwMethod(method, "Argument type expected to be Set<String>");

                Class cls = Util.getRawType(Util.getSingleParameterUpperBound((ParameterizedType)type));
                if (!cls.equals(String.class))
                    throwMethod(method, "Argument type expected to be Set<String>");
            }

            set(name, aClass, args[0]);
            return null;
        }
        else {
            final Object defValue = args != null && args.length > 0 ? args[0] : null;

            if (returnType.equals(Observable.class)) {
                final Class cls = Util.getRawType(Util.getSingleParameterUpperBound((ParameterizedType)genericReturnType));
                return Observable.create(new OnSharedPreferenceChangeListenerOnSubscribe(pref, name, new Func0() {
                    @Override
                    public Object call() {
                        return get(name, cls, defValue);
                    }
                }));
            }

            if (defValue != null && !returnType.isAssignableFrom(defValue.getClass()))
                throwMethod(method, "Value expected to be " + returnType.getName() + " but was " + defValue.getClass().getName());

            if (returnType.equals(Set.class)) {
                if (!(genericReturnType instanceof ParameterizedType))
                    throwMethod(method, "Return type expected to be Set<String>");

                Class cls = Util.getRawType(Util.getSingleParameterUpperBound((ParameterizedType)genericReturnType));
                if (!cls.equals(String.class))
                    throwMethod(method, "Return type expected to be Set<String>");
            }

            return get(name, returnType, defValue);
        }
    }

    static void throwMethod(Method method, String error) {
        throw new IllegalArgumentException("Method: " + method.getDeclaringClass().getName() + "." + method.getName() + "\n" + error);
    }

    static String getPreferenceName(Method method) {
        Preference preference = method.getAnnotation(Preference.class);
        if (preference != null)
            return preference.value();

        String name = method.getName();
        return (name.length() > 3 && name.startsWith(isSet(method) ? "set" : "get")) ?
            name.substring(3, 4).toLowerCase() + (name.length() > 4 ? name.substring(4) : "") :
            name;
    }

    static boolean isSet(Method method) {
        Class<?> returnType = method.getReturnType();
        return returnType.equals(Action1.class) || returnType.equals(Void.TYPE);
    }

    private Object get(String key, Class<?> cls, Object defValue) {

        if (!pref.contains(key))
            return defValue;

        if (cls.equals(String.class))
            return pref.getString(key, (String)defValue);

        else if (cls.equals(Set.class))
            return pref.getStringSet(key, (Set<String>)defValue);

        else if (cls.equals(Integer.class) || cls.equals(int.class))
            return pref.getInt(key, defValue == null ? 0 : (Integer)defValue);

        else if (cls.equals(Long.class) || cls.equals(long.class))
            return pref.getLong(key, defValue == null ? 0 : (Long)defValue);

        else if (cls.equals(Float.class) || cls.equals(float.class))
            return pref.getFloat(key, defValue == null ? 0 : (Float)defValue);

        else if (cls.equals(Boolean.class) || cls.equals(boolean.class))
            return pref.getBoolean(key, defValue == null ? false : (Boolean)defValue);

        else
            throw new IllegalArgumentException("Invalid shared preference type: " + cls.getName());
    }

    private void set(String key, Class<?> cls, Object value) {

        if (value == null)
            pref.edit().remove(key).apply();

        else {
            if (String.class.isAssignableFrom(cls))
                pref.edit().putString(key, (String)value).apply();

            else if (Set.class.isAssignableFrom(cls))
                pref.edit().putStringSet(key, (Set<String>)value).apply();

            else if (Integer.class.isAssignableFrom(cls) || int.class.equals(cls))
                pref.edit().putInt(key, value == null ? 0 : (Integer)value).apply();

            else if (Long.class.isAssignableFrom(cls) || long.class.equals(cls))
                pref.edit().putLong(key, value == null ? 0 : (Long)value).apply();

            else if (Float.class.isAssignableFrom(cls) || float.class.equals(cls))
                pref.edit().putFloat(key, value == null ? 0 : (Float)value).apply();

            else if (Boolean.class.isAssignableFrom(cls) || boolean.class.equals(cls))
                pref.edit().putBoolean(key, value == null ? false : (Boolean)value).apply();
            else
                throw new IllegalArgumentException("Invalid shared preference type: " + cls.getName());
        }
    }
}
