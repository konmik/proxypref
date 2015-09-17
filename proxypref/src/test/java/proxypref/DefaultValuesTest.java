package proxypref;

import android.content.SharedPreferences;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;

import proxypref.annotation.DefaultBoolean;
import proxypref.annotation.DefaultFloat;
import proxypref.annotation.DefaultInteger;
import proxypref.annotation.DefaultLong;
import proxypref.annotation.DefaultSet;
import proxypref.annotation.DefaultString;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DefaultValuesTest {

    interface Defaults {

        @DefaultString("value")
        String getString();

        @DefaultInteger(64)
        Integer getInteger();

        @DefaultLong(64)
        Long getLong();

        @DefaultFloat(64f)
        Float getFloat();

        @DefaultBoolean(true)
        Boolean getBoolean();

        @DefaultSet({"value"})
        Set<String> getSet();
    }

    @Test
    public void default_values_returned() throws Exception {
        SharedPreferences pref = Mockito.mock(SharedPreferences.class);
        Defaults test = ProxyPreferences.build(Defaults.class, pref);

        assertEquals("value", test.getString());
        assertEquals(64, (int)test.getInteger());
        assertEquals(64, (long)test.getLong());
        assertEquals(64, test.getFloat(), 0);
        assertEquals(true, test.getBoolean());
        assertArrayEquals(new Object[]{"value"}, test.getSet().toArray());
    }
}
