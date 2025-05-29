package net.gamrath.testpredictions;

import org.junit.jupiter.api.extension.*;

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
            int result = JOptionPane.showOptionDialog(
                    null,
                    "Do you predict that ALL tests will pass, or that ANY will fail?",
                    "Overall Test Prediction",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"All will pass", "Any will fail"},
                    "All will pass"
            );
            if (result == 0) {
                prediction = ALL_PASS;
            } else if (result == 1) {
                prediction = ANY_FAIL;
            } else if (result == -1) {
                prediction = SKIP;
            }else {
                throw new IllegalStateException("No prediction made. result=%d".formatted(result));
            }
        });
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        String uniqueId = context.getUniqueId();
        boolean testFailed = context.getExecutionException().isPresent();
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
    }
}
