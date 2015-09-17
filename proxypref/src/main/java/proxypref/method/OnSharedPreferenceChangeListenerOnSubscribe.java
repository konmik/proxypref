package proxypref.method;

import android.content.SharedPreferences;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.subscriptions.Subscriptions;

public class OnSharedPreferenceChangeListenerOnSubscribe implements Observable.OnSubscribe<Object> {

    private final SharedPreferences pref;
    private final String name;
    private final Func0 get;

    public OnSharedPreferenceChangeListenerOnSubscribe(SharedPreferences pref, String name, Func0 get) {
        this.pref = pref;
        this.name = name;
        this.get = get;
    }

    @Override
    public void call(final Subscriber<? super Object> subscriber) {
        final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(name))
                    subscriber.onNext(get.call());
            }
        };
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                pref.unregisterOnSharedPreferenceChangeListener(listener);
            }
        }));
        pref.registerOnSharedPreferenceChangeListener(listener);

        subscriber.onNext(get.call());
    }
}
