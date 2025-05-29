package net.gamrath.testpredictions;

import java.util.Collection;
import java.util.function.Predicate;

enum Prediction implements Predicate<Collection<TestResult>> {
    ALL_PASS, ANY_FAIL, SKIP;

    @Override
    public boolean test(Collection<TestResult> results) {
        return switch (this) {
            case ALL_PASS -> results.stream().allMatch(x -> x == TestResult.PASS);
            case ANY_FAIL -> results.stream().anyMatch(x -> x == TestResult.FAIL);
            case SKIP -> true;
        };
    }
}
