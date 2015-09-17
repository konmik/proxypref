package proxypref.method;

import android.content.SharedPreferences;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import proxypref.annotation.Preference;
import rx.Observable;
import rx.functions.Action1;

enum MethodType {

    GET(false) {
        @Override
        DataType getDataType(Method method) {
            return DataType.fromClass(method.getReturnType(), method.getGenericReturnType());
        }

        @Override
        public Object invoke(DataType dataType, String key, SharedPreferences pref, Object[] args, Object defValue) {
            Object value = pref.getAll().get(key);
            return value == null ? defValue : value;
        }
    },
    SET(true) {
        @Override
        DataType getDataType(Method method) {
            return DataType.fromClass(method.getParameterTypes()[0], method.getGenericParameterTypes()[0]);
        }

        @Override
        public Object invoke(DataType dataType, String key, SharedPreferences pref, Object[] args, Object defValue) {
            if (args[0] == null)
                pref.edit().remove(key).apply();
            else
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
        public Object invoke(final DataType dataType, final String key, final SharedPreferences pref, final Object[] args, final Object defValue) {
            return RxDelegate.createObservable(dataType, key, pref, args, defValue);
        }
    },
    ACTION(true) {
        @Override
        DataType getDataType(Method method) {
            return dataTypeFromGenericReturnType(method);
        }

        @Override
        public Object invoke(final DataType dataType, final String key, final SharedPreferences pref, final Object[] args, final Object defValue) {
            return RxDelegate.createAction1(dataType, key, pref, defValue);
        }
    };

    static MethodType from(Method method, boolean rx) {
        Class<?> returnType = method.getReturnType();
        boolean returnVoid = returnType.equals(Void.TYPE);
        int parameterCount = method.getParameterTypes().length;
        if (returnVoid && parameterCount == 1)
            return MethodType.SET;
        if (!returnVoid && parameterCount == 0) {
            if (rx) {
                if (returnType.equals(Action1.class))
                    return MethodType.ACTION;
                if (returnType.equals(Observable.class))
                    return MethodType.OBSERVABLE;
            }
            return MethodType.GET;
        }
        throw illegalMethodException(method, "Unable to detect a method type");
    }

    private static DataType dataTypeFromGenericReturnType(Method method) {
        Type returnType = method.getGenericReturnType();
        if (!(returnType instanceof ParameterizedType))
            throw illegalMethodException(method, "Invalid shared preferences type");
        ParameterizedType parameterizedType = (ParameterizedType)returnType;
        Type arg0 = parameterizedType.getActualTypeArguments()[0];
        return DataType.fromClass((Class<?>)(arg0 instanceof Class ? arg0 : ((ParameterizedType)arg0).getRawType()), arg0);
    }

    private final boolean isSet;

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

    private static IllegalArgumentException illegalMethodException(Method method, String error) {
        throw new IllegalArgumentException("Method: " + method.getDeclaringClass().getName() + "." + method.getName() + "\n" + error);
    }
}
