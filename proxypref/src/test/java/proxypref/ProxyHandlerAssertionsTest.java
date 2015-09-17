package proxypref;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

public class ProxyHandlerAssertionsTest {

    interface TotallyFailed {
        void no_arg_void_return();
        Integer arg_and_return(Integer x);
        void two_args(Integer x, Integer y);

        Double illegal_return_type();
        void illegal_arg_type(Double x);

        Set not_parametrized_return_set_type();
        void not_parametrized_argument_set_type(Set x);
        Set<Double> illegal_return_set_type();
        void illegal_argument_set_type(Set<Double> x);
    }

    @Rule public ExpectedException expected = ExpectedException.none();

    TotallyFailed test;

    @Before
    public void setUp() throws Exception {
        test = ProxyPreferences.build(TotallyFailed.class, null);
    }

    @After
    public void tearDown() throws Exception {
        ProxyHandler.clearCache();
    }

    @Test
    public void no_arg_void_return_throws() throws Exception {
        expect("Unable to detect a method type");
        test.no_arg_void_return();
    }

    @Test
    public void arg_and_return_throws() throws Exception {
        expect("Unable to detect a method type");
        test.arg_and_return(1);
    }

    @Test
    public void two_args_throws() throws Exception {
        expect("Unable to detect a method type");
        test.two_args(1, 1);
    }

    @Test
    public void illegal_return_type_throws() throws Exception {
        expect("Invalid shared preferences type");
        test.illegal_return_type();
    }

    @Test
    public void illegal_arg_type_throws() throws Exception {
        expect("Invalid shared preferences type");
        test.illegal_arg_type(1d);
    }

    @Test
    public void not_parametrized_return_set_type() throws Exception {
        expect("Invalid shared preferences type");
        test.not_parametrized_return_set_type();
    }

    @Test
    public void not_parametrized_argument_set_type() throws Exception {
        expect("Invalid shared preferences type");
        test.not_parametrized_argument_set_type(null);
    }

    @Test
    public void illegal_return_set_type_throws() throws Exception {
        expect("Invalid shared preferences type");
        test.illegal_return_set_type();
    }

    @Test
    public void illegal_argument_set_type_throws() throws Exception {
        expect("Invalid shared preferences type");
        test.illegal_argument_set_type(null);
    }

    private void expect(String substring) {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage(substring);
    }
}
