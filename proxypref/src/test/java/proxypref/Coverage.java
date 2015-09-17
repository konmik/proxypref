package proxypref;

import org.junit.Test;

import info.android15.proxypreferences.BuildConfig;

public class Coverage {
    @Test
    public void instantiate_utility_classes() throws Exception {
        new ProxyPreferences();
        new BuildConfig();
    }
}
