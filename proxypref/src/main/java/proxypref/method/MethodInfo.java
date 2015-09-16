package proxypref.method;

import android.content.SharedPreferences;

import java.lang.reflect.Method;

public class MethodInfo {

    private final MethodType methodType;
    private final DataType dataType;
    private final String key;
    private final Object defValue;

    public MethodInfo(Method method) {
        this.methodType = MethodType.from(method);
        this.dataType = methodType.getDataType(method);
        this.key = methodType.getKey(method);
        this.defValue = dataType.getDefaultValue(method);
    }

    public Object invoke(SharedPreferences pref, Object[] args) {
        return methodType.invoke(dataType, key, pref, args, defValue);
    }
}
