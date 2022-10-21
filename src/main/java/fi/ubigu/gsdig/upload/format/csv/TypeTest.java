package fi.ubigu.gsdig.upload.format.csv;

import java.util.function.Function;

public class TypeTest {

    private Class<?> type;
    private Function<String, Boolean> test;

    public TypeTest(Class<?> type, Function<String, Boolean> test) {
        this.type = type;
        this.test = test;
    }

    public Class<?> getType() {
        return type;
    }

    public Function<String, Boolean> getTest() {
        return test;
    }

    public boolean test(String s) {
        return test.apply(s);
    }

}
