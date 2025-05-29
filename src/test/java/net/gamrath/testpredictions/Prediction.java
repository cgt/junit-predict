package net.gamrath.testpredictions;

import java.util.Collection;
import java.util.function.Predicate;

enum Prediction implements Predicate<Collection<TestResult>> {
    ALL_PASS, ANY_FAIL, SKIP;

    @Override
    public boolean test(Collection<TestResult> outcomes) {
        if (this == ALL_PASS) {
            return outcomes.stream().allMatch(x -> x == TestResult.PASS);
        } else if (this == ANY_FAIL) {
            return outcomes.stream().anyMatch(x -> x == TestResult.FAIL);
        } else if (this == SKIP) {
            return true;
        }
        throw new IllegalStateException();
    }
}
