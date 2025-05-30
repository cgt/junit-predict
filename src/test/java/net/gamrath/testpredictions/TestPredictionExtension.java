package net.gamrath.testpredictions;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static net.gamrath.testpredictions.Prediction.*;

public class TestPredictionExtension implements BeforeAllCallback, AfterTestExecutionCallback, AfterAllCallback {

    private final Map<String, TestResult> resultByTestName = new HashMap<>();
    private Prediction prediction = null;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var result = JOptionPane.showOptionDialog(
                    null,
                    "Do you predict that ALL tests will PASS or that ANY will FAIL?",
                    "Call your shot!",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"FAIL", "PASS"},
                    null
            );
            if (result == 0) {
                prediction = ANY_FAIL;
            } else if (result == 1) {
                prediction = ALL_PASS;
            } else if (result == JOptionPane.CLOSED_OPTION) {
                prediction = SKIP;
            } else {
                throw new IllegalStateException("No prediction made. result=%d".formatted(result));
            }
        });
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        var uniqueId = context.getUniqueId();
        var testFailed = context.getExecutionException().isPresent();
        resultByTestName.put(uniqueId, testFailed ? TestResult.FAIL : TestResult.PASS);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        var hit = prediction.test(resultByTestName.values());
        JOptionPane.showMessageDialog(
                null,
                hit ? "Hit" : "Miss",
                "Prediction Result",
                hit ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
        );
        resultByTestName
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(x -> {
                    var testName = x.getKey();
                    var result = x.getValue();
                    System.out.printf("%s: %s%n", testName, result);
                });

        var testClass = context.getTestClass().map(x -> x.getCanonicalName()).orElseThrow();
        var log = "%s,%s,%s".formatted(testClass, prediction, hit);
        System.out.println(log);
    }
}
