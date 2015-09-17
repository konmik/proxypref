package proxypref.method;

import android.content.SharedPreferences;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;

class RxDelegate {

    static Object createAction1(final DataType dataType, final String key, final SharedPreferences pref, final Object defValue) {
        return new Action1() {
            @Override
            public void call(Object o) {
                MethodType.SET.invoke(dataType, key, pref, new Object[]{o}, defValue);
            }
        };
    }

    static Object createObservable(final DataType dataType, final String key, final SharedPreferences pref, final Object[] args, final Object defValue) {
        return Observable.create(new OnSharedPreferenceChangeListenerOnSubscribe(pref, key, new Func0() {
            @Override
            public Object call() {
                return MethodType.GET.invoke(dataType, key, pref, args, defValue);
            }
        }));
    }
}
