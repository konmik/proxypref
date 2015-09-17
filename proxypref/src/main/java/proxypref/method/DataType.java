package proxypref.method;

import android.content.SharedPreferences;

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

import static java.util.Arrays.asList;

enum DataType {
    STRING {
        @Override
        void put(SharedPreferences pref, String key, Object value) {
            pref.edit().putString(key, (String)value).apply();
        }

        @Override
        Object getDefaultValue(Method method) {
            DefaultString annotation = method.getAnnotation(DefaultString.class);
            return annotation == null ? null : annotation.value();
        }
    },
    INTEGER {
        @Override
        void put(SharedPreferences pref, String key, Object value) {
            pref.edit().putInt(key, (int)value).apply();
        }

        @Override
        Object getDefaultValue(Method method) {
            DefaultInteger annotation = method.getAnnotation(DefaultInteger.class);
            return annotation == null ? null : annotation.value();
        }
    },
    LONG {
        @Override
        void put(SharedPreferences pref, String key, Object value) {
            pref.edit().putLong(key, (long)value).apply();
        }

        @Override
        Object getDefaultValue(Method method) {
            DefaultLong annotation = method.getAnnotation(DefaultLong.class);
            return annotation == null ? null : annotation.value();
        }
    },
    FLOAT {
        @Override
        void put(SharedPreferences pref, String key, Object value) {
            pref.edit().putFloat(key, (float)value).apply();
        }

        @Override
        Object getDefaultValue(Method method) {
            DefaultFloat annotation = method.getAnnotation(DefaultFloat.class);
            return annotation == null ? null : annotation.value();
        }
    },
    BOOLEAN {
        @Override
        void put(SharedPreferences pref, String key, Object value) {
            pref.edit().putBoolean(key, (boolean)value).apply();
        }

        @Override
        Object getDefaultValue(Method method) {
            DefaultBoolean annotation = method.getAnnotation(DefaultBoolean.class);
            return annotation == null ? null : annotation.value();
        }
    },
    SET {
        @Override
        void put(SharedPreferences pref, String key, Object value) {
            pref.edit().putStringSet(key, (Set<String>)value).apply();
        }

        @Override
        Object getDefaultValue(Method method) {
            DefaultSet annotation = method.getAnnotation(DefaultSet.class);
            return annotation == null ? null : Collections.unmodifiableSet(new HashSet<>(asList(annotation.value())));
        }
    };

    static DataType fromClass(Class<?> cls, Type type) {
        if (cls.equals(String.class))
            return STRING;
        if (cls.equals(Integer.class))
            return INTEGER;
        if (cls.equals(Long.class))
            return LONG;
        if (cls.equals(Float.class))
            return FLOAT;
        if (cls.equals(Boolean.class))
            return BOOLEAN;
        if (cls.equals(Set.class) && type instanceof ParameterizedType &&
            ((ParameterizedType)type).getActualTypeArguments()[0].equals(String.class))
            return SET;
        throw new IllegalArgumentException("Invalid shared preferences type: " + type.toString());
    }

    abstract void put(SharedPreferences pref, String key, Object value);
    abstract Object getDefaultValue(Method method);
}
