package fi.ubigu.gsdig.upload.format.csv;

import java.util.List;

public class TypeTester {

    private List<TypeTest> tests;
    private final int n;
    private int i;
    private boolean allEmpty;

    public TypeTester(List<TypeTest> tests) {
        this.tests = tests;
        this.n = tests.size() - 1;
        this.i = 0;
        this.allEmpty = true;
    }

    public void test(String s) {
        if (!s.isEmpty()) {
            allEmpty = false;
        }
        while (i < n && !tests.get(i).test(s)) {
            i++;
        }
    }
    
    public TypeTest getDetected() {
        return tests.get(i);
    }

    public Class<?> getDetectedType() {
        return tests.get(i).getType();
    }

    public boolean isAllEmpty() {
        return allEmpty;
    }

}
