package net.gamrath.junitpredict;

import org.junit.jupiter.api.Test;

import java.util.List;

import static net.gamrath.junitpredict.Prediction.ALL_PASS;
import static net.gamrath.junitpredict.Prediction.ANY_FAIL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PredictionTest {
    @Test
    void allPassHit() {
        assertTrue(ALL_PASS.test(List.of(TestResult.PASS, TestResult.PASS)));
    }

    @Test
    void allPassMiss() {
        assertFalse(ALL_PASS.test(List.of(TestResult.PASS, TestResult.FAIL)));
    }

    @Test
    void anyFailHit() {
        assertTrue(ANY_FAIL.test(List.of(TestResult.FAIL)));
        assertTrue(ANY_FAIL.test(List.of(TestResult.PASS, TestResult.FAIL)));
    }

    @Test
    void anyFailMiss() {
        assertFalse(ANY_FAIL.test(List.of(TestResult.PASS)));
        assertFalse(ANY_FAIL.test(List.of(TestResult.PASS, TestResult.PASS)));
    }

    @Test
    void allPassWhenThereAreNoTestsIsAHit() {
        assertTrue(ALL_PASS.test(List.of()));
    }

    @Test
    void anyFailWhenThereAreNoTestsIsAMiss() {
        assertFalse(ANY_FAIL.test(List.of()));
    }

    @Test
    void skipIsAlwaysAHit() {
        assertTrue(Prediction.SKIP.test(List.of()));
        assertTrue(Prediction.SKIP.test(List.of(TestResult.PASS)));
        assertTrue(Prediction.SKIP.test(List.of(TestResult.FAIL)));
        assertTrue(Prediction.SKIP.test(List.of(TestResult.PASS, TestResult.FAIL)));
    }
}