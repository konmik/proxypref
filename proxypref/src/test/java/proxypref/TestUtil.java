package proxypref;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestUtil {
    static <T> Answer<T> answerDefault(final String name) {
        return new Answer<T>() {
            @Override
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                return name.equals(invocationOnMock.getArguments()[0]) ? (T)invocationOnMock.getArguments()[1] : null;
            }
        };
    }

    static <T> Answer<T> answerValue(final String name, final T value) {
        return new Answer<T>() {
            @Override
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                return name.equals(invocationOnMock.getArguments()[0]) ? value : null;
            }
        };
    }
}
