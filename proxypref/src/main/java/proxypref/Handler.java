package proxypref;

import android.content.SharedPreferences;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import proxypref.annotation.DefaultBoolean;
import proxypref.annotation.DefaultFloat;
import proxypref.annotation.DefaultInteger;
import proxypref.annotation.DefaultLong;
import proxypref.annotation.DefaultSet;
import proxypref.annotation.DefaultString;
import proxypref.annotation.Preference;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;

import static java.util.Arrays.asList;

class Handler implements InvocationHandler {

    private final SharedPreferences pref;

    public Handler(SharedPreferences pref) {
        this.pref = pref;
    }

    private static HashSet<Method> assertedMethods = new HashSet<>();

    enum MethodType {
        GET(false) {
            @Override
            Object invoke(Handler handler, Method method, Object[] args) {
                if (!assertedMethods.contains(method)) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType.equals(Set.class))
                        assertSetString(method, method.getGenericReturnType());
                    assertedMethods.add(method);
                }
                return handler.get(getPreferenceKey(this, method), method.getReturnType(), method);
            }
        },
        SET(true) {
            @Override
            Object invoke(Handler handler, Method method, Object[] args) {
                handler.set(getPreferenceKey(this, method), getDataType(method), args[0]);
                return null;
            }

            Class<?> getDataType(Method method) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 0)
                    throwMethod(method, "Expected to have at least one parameter");

                Class<?> parameterType = parameterTypes[0];
                if (parameterType.equals(Set.class))
                    assertSetString(method, method.getGenericParameterTypes()[0]);
                return parameterType;
            }
        },
        OBSERVABLE(false) {
            @Override
            Object invoke(final Handler handler, final Method method, Object[] args) {
                final String key = getPreferenceKey(this, method);
                final Class cls = dataTypeFromGenericReturnType(method);
                if (cls.equals(Set.class)) {
                    assertParametrized(method, method.getGenericReturnType());
                    ParameterizedType t = (ParameterizedType)method.getGenericReturnType();
                    assertSetString(method, t.getActualTypeArguments()[0]);
                }
                return Observable.create(new OnSharedPreferenceChangeListenerOnSubscribe(handler.pref, key, new Func0() {
                    @Override
                    public Object call() {
                        return handler.get(key, cls, method);
                    }
                }));
            }
        },
        ACTION(true) {
            @Override
            Object invoke(final Handler handler, Method method, Object[] args) {
                // TODO: assert set<string>
                final String key = getPreferenceKey(this, method);
                final Class cls = dataTypeFromGenericReturnType(method);
                return new Action1() {
                    @Override
                    public void call(Object o) {
                        handler.set(key, cls, o);
                    }
                };
            }
        };

        private static void assertSetString(Method method, Type type) {
            assertParametrized(method, type);

            Class cls = Util.getRawType(Util.getSingleParameterUpperBound((ParameterizedType)type));
            if (!cls.equals(String.class))
                throwMethod(method, "Parameter type expected to be Set<String>");
        }

        private static void assertParametrized(Method method, Type type) {
            if (!(type instanceof ParameterizedType))
                throwMethod(method, "Parameter type expected to be Set<String>");
        }

        private static Class<?> dataTypeFromGenericReturnType(Method method) {
            final Type genericReturnType = method.getGenericReturnType();
            return Util.getRawType(Util.getSingleParameterUpperBound((ParameterizedType)genericReturnType));
        }

        public final boolean isSet;

        MethodType(boolean isSet) {
            this.isSet = isSet;
        }

        abstract Object invoke(Handler handler, Method method, Object[] args);
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable {
        return getMethodType(method).invoke(this, method, args);
    }

    static String getPreferenceKey(MethodType type, Method method) {
        Preference preference = method.getAnnotation(Preference.class);
        if (preference != null)
            return preference.value();

        String name = method.getName();
        return (name.length() > 3 && name.startsWith(type.isSet ? "set" : "get")) ?
            name.substring(3, 4).toLowerCase() + (name.length() > 4 ? name.substring(4) : "") :
            name;
    }

    static MethodType getMethodType(Method method) {
        Class<?> returnType = method.getReturnType();
        int parameterCount = method.getParameterTypes().length;
        if (parameterCount == 1) {
            if (returnType.equals(Void.TYPE))
                return MethodType.SET;
        }
        else if (parameterCount == 0) {
            if (returnType.equals(Action1.class))
                return MethodType.ACTION;
            if (returnType.equals(Observable.class))
                return MethodType.OBSERVABLE;
            return MethodType.GET;
        }
        throw illegalMethodException(method, "Unable to detect a method type");
    }

    private synchronized Object get(String key, Class<?> cls, Method method) {

        if (cls.equals(String.class)) {
            DefaultString annotation = method.getAnnotation(DefaultString.class);
            if (!pref.contains(key)) return annotation == null ? null : annotation.value();
            return pref.getString(key, annotation == null ? "" : annotation.value());
        }
        else if (cls.equals(Integer.class)) {
            DefaultInteger annotation = method.getAnnotation(DefaultInteger.class);
            if (!pref.contains(key)) return annotation == null ? null : annotation.value();
            return pref.getInt(key, annotation == null ? 0 : annotation.value());
        }
        else if (cls.equals(Long.class)) {
            DefaultLong annotation = method.getAnnotation(DefaultLong.class);
            if (!pref.contains(key)) return annotation == null ? null : annotation.value();
            return pref.getLong(key, annotation == null ? 0 : annotation.value());
        }
        else if (cls.equals(Float.class)) {
            DefaultFloat annotation = method.getAnnotation(DefaultFloat.class);
            if (!pref.contains(key)) return annotation == null ? null : annotation.value();
            return pref.getFloat(key, annotation == null ? 0 : annotation.value());
        }
        else if (cls.equals(Boolean.class)) {
            DefaultBoolean annotation = method.getAnnotation(DefaultBoolean.class);
            if (!pref.contains(key)) return annotation == null ? null : annotation.value();
            return pref.getBoolean(key, annotation == null ? false : annotation.value());
        }
        else if (cls.equals(Set.class)) {
            DefaultSet annotation = method.getAnnotation(DefaultSet.class);
            if (!pref.contains(key)) return annotation == null ? null : new HashSet<>(asList(annotation.value()));
            return pref.getStringSet(key, annotation == null ? Collections.<String>emptySet() : new HashSet<>(asList(annotation.value())));
        }
        else
            throw new IllegalArgumentException("Invalid shared preferences type: " + cls.getName());
    }

    private synchronized void set(String key, Class<?> cls, Object value) {

        if (value == null)
            pref.edit().remove(key).apply();

        else if (cls.equals(String.class))
            pref.edit().putString(key, (String)value).apply();

        else if (cls.equals(Set.class))
            pref.edit().putStringSet(key, (Set<String>)value).apply();

        else if (cls.equals(Integer.class))
            pref.edit().putInt(key, (Integer)value).apply();

        else if (cls.equals(Long.class))
            pref.edit().putLong(key, (Long)value).apply();

        else if (cls.equals(Float.class))
            pref.edit().putFloat(key, (Float)value).apply();

        else if (cls.equals(Boolean.class))
            pref.edit().putBoolean(key, (Boolean)value).apply();

        else
            throw new IllegalArgumentException("Invalid shared preferences type: " + cls.getName());
    }

    private static void throwMethod(Method method, String error) {
        throw new IllegalArgumentException("Method: " + method.getDeclaringClass().getName() + "." + method.getName() + "\n" + error);
    }

    private static IllegalArgumentException illegalMethodException(Method method, String error) {
        throw new IllegalArgumentException("Method: " + method.getDeclaringClass().getName() + "." + method.getName() + "\n" + error);
    }
}
