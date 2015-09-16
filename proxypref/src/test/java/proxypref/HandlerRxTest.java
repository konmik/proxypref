package proxypref;

import android.content.SharedPreferences;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import proxypref.annotation.DefaultString;
import rx.Observable;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static proxypref.TestUtil.answerDefault;
import static proxypref.TestUtil.answerValue;

public class HandlerRxTest {

    interface StringTest {
        Observable<String> testObservable();
        @DefaultString("1")
        Observable<String> testObservableDefault1();
        Observable<Set<Integer>> throwIllegalType();
        Observable<Set<String>> dontThrowIllegalType();
        Observable<Double> throwIllegalDouble();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailIllegalType() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        StringTest test = ProxyPreferences.build(StringTest.class, pref);
        test.throwIllegalType();
    }

    @Test
    public void testDontThrowIllegalType() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        StringTest test = ProxyPreferences.build(StringTest.class, pref);
        test.dontThrowIllegalType();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowIllegalDouble() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        StringTest test = ProxyPreferences.build(StringTest.class, pref);
        test.throwIllegalDouble();
    }

    @Test
    public void testObservable() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        when(pref.contains("testObservable")).thenReturn(true);
        when(pref.getString(anyString(), anyString()))
            .thenAnswer(answerValue("testObservable", "value"));

        StringTest test = ProxyPreferences.build(StringTest.class, pref);

        final AtomicReference<SharedPreferences.OnSharedPreferenceChangeListener> listener = new AtomicReference<>();
        final AtomicReference<SharedPreferences.OnSharedPreferenceChangeListener> unregisteredListener = new AtomicReference<>();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                listener.set((SharedPreferences.OnSharedPreferenceChangeListener)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(pref).registerOnSharedPreferenceChangeListener(any(SharedPreferences.OnSharedPreferenceChangeListener.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                assertEquals(invocationOnMock.getArguments()[0], listener.get());
                assertNotNull(invocationOnMock.getArguments()[0]);
                unregisteredListener.set((SharedPreferences.OnSharedPreferenceChangeListener)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(pref).unregisterOnSharedPreferenceChangeListener(any(SharedPreferences.OnSharedPreferenceChangeListener.class));

        TestSubscriber<String> subscriber = new TestSubscriber<>();

        Subscription subscription = test.testObservable().subscribe(subscriber);
        subscriber.assertReceivedOnNext(Collections.singletonList("value"));

        when(pref.getString(anyString(), anyString()))
            .thenAnswer(answerValue("testObservable", "value2"));
        listener.get().onSharedPreferenceChanged(pref, "testObservable");
        subscriber.assertReceivedOnNext(asList("value", "value2"));

        listener.get().onSharedPreferenceChanged(pref, "some other key");
        subscriber.assertReceivedOnNext(asList("value", "value2"));

        subscription.unsubscribe();

        subscriber.assertNoTerminalEvent();
        verify(pref, times(1)).unregisterOnSharedPreferenceChangeListener(listener.get());
    }

    @Test
    public void testObservableDefault() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        when(pref.getString(anyString(), anyString()))
            .thenAnswer(answerDefault("testObservable"));
        StringTest test = ProxyPreferences.build(StringTest.class, pref);

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        test.testObservableDefault1().subscribe(subscriber);
        subscriber.assertReceivedOnNext(Collections.singletonList("1"));
    }
}
