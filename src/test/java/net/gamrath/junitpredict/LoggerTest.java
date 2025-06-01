package net.gamrath.junitpredict;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import java.util.List;

class LoggerTest {
    @Test
    void name() {
        final var log = Logger.createLog(List.of(), false, Prediction.ALL_PASS);
        Approvals.verify(String.join("\n", log) + "\n");
    }
}