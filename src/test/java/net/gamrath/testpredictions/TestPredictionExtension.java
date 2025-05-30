package net.gamrath.testpredictions;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static net.gamrath.testpredictions.Prediction.*;

public class TestPredictionExtension implements BeforeAllCallback, AfterTestExecutionCallback, AfterAllCallback {

    private final Map<String, TestResult> resultByTestName = new HashMap<>();
    private Prediction prediction = null;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final var result = JOptionPane.showOptionDialog(
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
        final var uniqueId = context.getUniqueId();
        final var testFailed = context.getExecutionException().isPresent();
        resultByTestName.put(uniqueId, testFailed ? TestResult.FAIL : TestResult.PASS);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        final var testClass = context.getRequiredTestClass().getCanonicalName();
        final var logPath = Path.of("predictions-%s.csv".formatted(testClass));

        int hits = 0;
        int misses = 0;

        try {
            final var lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
            for (String line : lines) {
                final var parts = line.split(",");
                final var hit = Boolean.parseBoolean(parts[1]);
                if (hit) {
                    hits++;
                } else {
                    misses++;
                }
            }
            System.out.printf("Previous predictions for %s: %d hits, %d misses%n", testClass, hits, misses);
        } catch (NoSuchFileException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        final var hit = prediction.test(resultByTestName.values());
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
                    final var testName = x.getKey();
                    final var result = x.getValue();
                    System.out.printf("%s: %s%n", testName, result);
                });

        final var log = "%s,%s".formatted(prediction, hit);
        try {
            Files.writeString(
                    logPath,
                    log + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.printf("Failed to write prediction log: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }
}
