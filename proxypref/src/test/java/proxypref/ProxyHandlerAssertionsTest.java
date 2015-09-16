package proxypref;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

import rx.Observable;
import rx.functions.Action1;

public class ProxyHandlerAssertionsTest {

    interface TotallyFailed {

        void methodTypeCanNotBeDetected();
        Integer methodTypeCanNotBeDetectedArgumentAndReturn(Integer x);
        void methodTypeCanNotBeDetectedTooManyArguments(Integer x, Integer y);

        Double illegalReturnType();
        void illegalSetArgument(Double x);

        Set<Double> illegalReturnSetParameter();
        void illegalSetArgumentParameter(Set<Double> x);

        Observable<Double> illegalObservableType();
        Observable<Set<Double>> illegalObservableSetParameter();

        Action1<Double> illegalActionType();
        Action1<Set<Double>> illegalActionSetParameter();
    }

    TotallyFailed test;

    @Rule public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        test = ProxyPreferences.build(TotallyFailed.class, null);
    }

    @Test
    public void test_methodTypeCanNotBeDetected() throws Exception {
        expect("Unable to detect a method type");
        test.methodTypeCanNotBeDetected();
    }

    @Test
    public void test_methodTypeCanNotBeDetectedArgumentAndReturn() throws Exception {
        expect("Unable to detect a method type");
        test.methodTypeCanNotBeDetectedArgumentAndReturn(1);
    }

    @Test
    public void test_methodTypeCanNotBeDetectedTooManyArguments() throws Exception {
        expect("Unable to detect a method type");
        test.methodTypeCanNotBeDetectedTooManyArguments(1, 1);
    }

    @Test
    public void test_illegalReturnType() throws Exception {
        expect("Invalid shared preferences type");
        test.illegalReturnType();
    }

    @Test
    public void test_illegalSetArgument() throws Exception {
        expect("Invalid shared preferences type");
        test.illegalSetArgument(1d);
    }

    @Test
    public void test_illegalReturnSetParameter() throws Exception {
        expect("Set<String>");
        test.illegalReturnSetParameter();
    }

    @Test
    public void test_illegalSetArgumentParameter() throws Exception {
        expect("Set<String>");
        test.illegalSetArgumentParameter(null);
    }

    @Test
    public void test_illegalObservableType() throws Exception {
        expect("Invalid shared preferences type");
        test.illegalObservableType();
    }

    @Test
    public void test_illegalObservableSetParameter() throws Exception {
        expect("Set<String>");
        test.illegalObservableSetParameter();
    }

    @Test
    public void test_illegalActionType() throws Exception {
        expect("Invalid shared preferences type");
        test.illegalActionType();
    }

    @Test
    public void test_illegalActionSetParameter() throws Exception {
        expect("Set<String>");
        test.illegalActionSetParameter();
    }

    private void expect(String substring) {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage(substring);
    }
}
