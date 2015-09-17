package proxypref;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

import rx.Observable;
import rx.functions.Action1;

public class ProxyHandlerRxAssertionsTest {

    interface TotallyFailed {
        Observable<Double> illegal_observable_type();
        Observable<Set<Double>> illegal_observable_set_type();
        Observable<Set> not_parametrized_observable_set_type();

        Action1 no_action_type();
        Action1<Double> illegal_action_type();
        Action1<Set<Double>> illegal_action_set_type();
        Action1<Set> not_parametrized_action_set_type();
    }

    @Rule public ExpectedException expected = ExpectedException.none();

    TotallyFailed test;

    @Before
    public void setUp() throws Exception {
        test = ProxyPreferences.buildWithRx(TotallyFailed.class, null);
    }

    @After
    public void tearDown() throws Exception {
        ProxyHandler.clearCache();
    }

    @Test
    public void illegal_observable_type() throws Exception {
        expect("Invalid shared preferences type");
        test.illegal_observable_type();
    }

    @Test
    public void illegal_observable_set_type() throws Exception {
        expect("Invalid shared preferences type");
        test.illegal_observable_set_type();
    }

    @Test
    public void not_parametrized_observable_set_type() throws Exception {
        expect("Invalid shared preferences type");
        test.not_parametrized_observable_set_type();
    }

    @Test
    public void no_action_type() throws Exception {
        expect("Invalid shared preferences type");
        test.no_action_type();
    }

    @Test
    public void illegal_action_type() throws Exception {
        expect("Invalid shared preferences type");
        test.illegal_action_type();
    }

    @Test
    public void illegal_action_set_type() throws Exception {
        expect("Invalid shared preferences type");
        test.illegal_action_set_type();
    }

    @Test
    public void not_parametrized_action_set_type() throws Exception {
        expect("Invalid shared preferences type");
        test.not_parametrized_action_set_type();
    }

    private void expect(String substring) {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage(substring);
    }
}
