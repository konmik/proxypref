package proxypref.method;

import android.content.SharedPreferences;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import proxypref.annotation.Preference;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;

enum MethodType {

    GET(false) {
        @Override
        DataType getDataType(Method method) {
            return DataType.fromClass(method.getReturnType(), method.getGenericReturnType());
        }

        @Override
        public Object invoke(DataType dataType, String key, SharedPreferences pref, Object[] args, Object defValue) {
            return pref.contains(key) ? dataType.get(pref, key, defValue) : defValue;
        }
    },
    SET(true) {
        @Override
        DataType getDataType(Method method) {
            return DataType.fromClass(method.getParameterTypes()[0], method.getGenericParameterTypes()[0]);
        }

        @Override
        public Object invoke(DataType dataType, String key, SharedPreferences pref, Object[] args, Object defValue) {
            dataType.put(pref, key, args[0]);
            return null;
        }
    },
    OBSERVABLE(false) {
        @Override
        DataType getDataType(Method method) {
            return dataTypeFromGenericReturnType(method);
        }

        @Override
        public Object invoke(final DataType dataType, final String key, final SharedPreferences pref, Object[] args, final Object defValue) {
            return Observable.create(new OnSharedPreferenceChangeListenerOnSubscribe(pref, key, new Func0() {
                @Override
                public Object call() {
                    return pref.contains(key) ? dataType.get(pref, key, defValue) : null;
                }
            }));
        }
    },
    ACTION(true) {
        @Override
        DataType getDataType(Method method) {
            return dataTypeFromGenericReturnType(method);
        }

        @Override
        public Object invoke(final DataType dataType, final String key, final SharedPreferences pref, Object[] args, Object defValue) {
            return new Action1() {
                @Override
                public void call(Object o) {
                    dataType.put(pref, key, o);
                }
            };
        }
    };

    static MethodType from(Method method) {
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
        throw Util.illegalMethodException(method, "Unable to detect a method type");
    }

    private static void assertParametrized(Method method, Type type) {
        if (!(type instanceof ParameterizedType))
            Util.throwMethod(method, "Parameter type expected to be Set<String>");
    }

    private static DataType dataTypeFromGenericReturnType(Method method) {
        Type returnType = method.getGenericReturnType();
        assertParametrized(method, returnType);
        ParameterizedType parameterizedType = (ParameterizedType)returnType;
        return DataType.fromClass((Class<?>)parameterizedType.getActualTypeArguments()[0], parameterizedType.getActualTypeArguments()[0]);
    }

    public final boolean isSet;

    MethodType(boolean isSet) {
        this.isSet = isSet;
    }

    abstract DataType getDataType(Method method);
    abstract Object invoke(DataType dataType, String key, SharedPreferences pref, Object[] args, Object defValue);

    String getKey(Method method) {
        Preference preference = method.getAnnotation(Preference.class);
        if (preference != null)
            return preference.value();

        String name = method.getName();
        return (name.length() > 3 && name.startsWith(isSet ? "set" : "get")) ?
            name.substring(3, 4).toLowerCase() + (name.length() > 4 ? name.substring(4) : "") :
            name;

    }
}
