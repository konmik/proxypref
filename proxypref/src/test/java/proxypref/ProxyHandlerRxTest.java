package proxypref;

import android.content.SharedPreferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.observers.TestSubscriber;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static proxypref.TestUtil.mockMapValue;

public class ProxyHandlerRxTest {

    interface StringTest {
        Action1<String> action();
        Observable<String> observable();
    }

    SharedPreferences pref;
    StringTest test;

    @Before
    public void setUp() throws Exception {
        pref = Mockito.mock(SharedPreferences.class);
        test = ProxyPreferences.buildWithRx(StringTest.class, pref);
    }

    @After
    public void tearDown() throws Exception {
        ProxyHandler.clearCache();
    }

    @Test
    public void action_sets_value() throws Exception {
        SharedPreferences.Editor editor = TestUtil.mockEditor(pref);

        test.action().call("value");

        InOrder order = inOrder(pref, editor);
        order.verify(pref, times(1)).edit();
        order.verify(editor, times(1)).putString("action", "value");
        order.verify(editor, times(1)).apply();
    }

    @Test
    public void observable_returns_an_existing_value_immediately() throws Exception {
        Map map = mockMapValue("observable", "value1");
        when(pref.getAll()).thenReturn(map);

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        test.observable().subscribe(subscriber);
        subscriber.assertReceivedOnNext(singletonList("value1"));
    }

    @Test
    public void observable_returns_the_next_value_on_update() throws Exception {
        Map map = mockMapValue("observable", "value1");
        when(pref.getAll()).thenReturn(map);

        final AtomicReference<SharedPreferences.OnSharedPreferenceChangeListener> register = new AtomicReference<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                register.set((SharedPreferences.OnSharedPreferenceChangeListener)invocation.getArguments()[0]);
                return null;
            }
        }).when(pref).registerOnSharedPreferenceChangeListener(any(SharedPreferences.OnSharedPreferenceChangeListener.class));

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        test.observable().subscribe(subscriber);

        map = mockMapValue("observable", "value2");
        when(pref.getAll()).thenReturn(map);
        register.get().onSharedPreferenceChanged(pref, "observable");

        subscriber.assertReceivedOnNext(asList("value1", "value2"));
    }

    @Test
    public void observable_unregisters_on_unsubscribe() throws Exception {
        final AtomicReference<SharedPreferences.OnSharedPreferenceChangeListener> unregister = new AtomicReference<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                unregister.set((SharedPreferences.OnSharedPreferenceChangeListener)invocation.getArguments()[0]);
                return null;
            }
        }).when(pref).unregisterOnSharedPreferenceChangeListener(any(SharedPreferences.OnSharedPreferenceChangeListener.class));

        Subscription subscription = test.observable().subscribe();

        subscription.unsubscribe();
        assertNotNull(unregister.get());
    }

    @Test
    public void observable_returns_null_if_no_data() throws Exception {
        Map map = new HashMap();
        when(pref.getAll()).thenReturn(map);

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        test.observable().subscribe(subscriber);
        subscriber.assertReceivedOnNext(Collections.<String>singletonList(null));
    }
}
