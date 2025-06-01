package net.gamrath.junitpredict;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A JUnit 5 extension that prompts the user to predict the outcome of tests before they run.
 * It records whether the prediction was a hit or a miss and writes the results to a log file
 * in the working directory (typically the project root).
 */
public class Predict implements BeforeAllCallback, AfterTestExecutionCallback, AfterAllCallback {

    private final Map<String, TestResult> resultByTestName = new HashMap<>();
    private Prediction prediction = null;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        promptForPrediction2();
    }

    private void promptForPrediction2() throws InterruptedException, InvocationTargetException {
        var p = new AtomicReference<Prediction>();
        SwingUtilities.invokeAndWait(() -> {
            final var choice = JOptionPane.showOptionDialog(null, "Do you predict that ALL tests will PASS or that ANY will FAIL?", "Call your shot!", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"FAIL", "PASS"}, null);
            Prediction result;
            if (choice == 0) {
                result = Prediction.ANY_FAIL;
            } else if (choice == 1) {
                result = Prediction.ALL_PASS;
            } else if (choice == JOptionPane.CLOSED_OPTION) {
                result = Prediction.SKIP;
            } else {
                throw new IllegalStateException("No prediction made. result=%d".formatted(choice));
            }
            p.set(result);
        });
        this.prediction = p.get();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        final var uniqueId = context.getUniqueId();
        final var testFailed = context.getExecutionException().isPresent();
        resultByTestName.put(uniqueId, testFailed ? TestResult.FAIL : TestResult.PASS);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    public void afterAll(ExtensionContext context) {
        final var testClass = context.getRequiredTestClass().getCanonicalName();
        final var logPath = Path.of("predictions-%s.csv".formatted(testClass));

        List<String> lines = Collections.emptyList();
        try {
            lines = Files
                    .readAllLines(logPath, StandardCharsets.UTF_8)
                    .stream()
                    .filter(line -> !line.startsWith("STATS:"))
                    .toList();
        } catch (NoSuchFileException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        var hits = 0;
        var misses = 0;
        for (final var line : lines) {
            final var parts = line.split(",");
            final var hit = Boolean.parseBoolean(parts[1]);
            if (hit) {
                hits++;
            } else {
                misses++;
            }
        }

        final var hit = prediction.test(resultByTestName.values());
        JOptionPane.showMessageDialog(
                null,
                hit ? "Hit" : "Miss",
                "Prediction Result",
                hit ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
        );

        final var log = "%s,%s".formatted(prediction, hit);
        final var newLines = new ArrayList<>(lines);
        newLines.add(log);
        newLines.add("STATS: hits=%d, misses=%d".formatted(hits + (hit ? 1 : 0), misses + (hit ? 0 : 1)));
        try {
            Files.writeString(
                    logPath,
                    String.join(System.lineSeparator(), newLines) + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            System.err.printf("Failed to write prediction log: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }
}
