# ProxyPref

ProxyPref is a simple Android library, which allows to easily access SharedPreferences.

### Definition

Define an interface with types that are supported by `SharedPreferences`.

Keys are method names from the interface, but without `set`/`get` prefixes.

``` java
interface MyPreferences {

    // access with get/set prefix
    String getTestString();         // key = testString
    void setTestString(String x);   // key = testString

    // without get/set prefix
    Integer testInteger();          // key = testInteger
    void testInteger(int x);        // key = testInteger

    // get with default value
    Integer getValue(int defaultValue);     // key = value

    // observe with rx.Observable
    Observable<Integer> lastSelectedItem(); // key = lastSelectedItem

    // set with rx.functions.Action1
    Action1<Integer> setLastSelectedItem(); // key = lastSelectedItem

    // ProGuard ready key
    @Preference("username")
    String username();     // key = username
}
```

RxJava `rx.Observable` is supported.

### Usage

One line initialization.

``` java
MyPreferences pref = ProxyPreferences.build(MyPreferences.class, getSharedPreferences("preferences", 0));
pref.username();
```

### Dependency

``` guava
compile 'info.android15.proxypref:proxypref:0.1.0'
```

