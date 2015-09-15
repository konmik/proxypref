package proxypref;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

class Util {
    // This method is copyright 2004 Square, Inc. and is taken from Retrofit under the Apache 2.0 license.
    static Type getSingleParameterUpperBound(ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (types.length != 1)
            throw new IllegalArgumentException(
                "Expected one type argument but got: " + Arrays.toString(types));
        Type paramType = types[0];
        if (paramType instanceof WildcardType)
            return ((WildcardType)paramType).getUpperBounds()[0];
        return paramType;
    }

    // This method is copyright 2008 Google Inc. and is taken from Gson under the Apache 2.0 license.
    static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>)
            return (Class<?>)type;
        else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>)rawType;
        }
        else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType)type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        else if (type instanceof TypeVariable)
            return Object.class;
        else if (type instanceof WildcardType)
            return getRawType(((WildcardType)type).getUpperBounds()[0]);
        else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + className);
        }
    }
}
