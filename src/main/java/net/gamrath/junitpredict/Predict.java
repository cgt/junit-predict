package net.gamrath.junitpredict;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * A JUnit 5 extension that prompts the user to predict the outcome of tests before they run.
 * It records whether the prediction was a hit or a miss and writes the results to a log file
 * in the working directory (typically the project root).
 */
public class Predict implements BeforeAllCallback, AfterTestExecutionCallback, AfterAllCallback {

    private final Map<String, TestResult> resultByTestName = new HashMap<>();
    private final UI ui = new SwingUI();
    private Prediction prediction = null;

    @Override
    public void beforeAll(ExtensionContext context) {
        this.prediction = ui.promptForPrediction();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        recordTestResults(context);
    }

    private void recordTestResults(ExtensionContext context) {
        final var uniqueId = context.getUniqueId();
        final var testFailed = context.getExecutionException().isPresent();
        resultByTestName.put(uniqueId, testFailed ? TestResult.FAIL : TestResult.PASS);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    public void afterAll(ExtensionContext context) {
        final var testClass = context.getRequiredTestClass().getCanonicalName();
        final var logPath = Path.of("predictions-%s.csv".formatted(testClass));

        final var lines = readLogFile(logPath);
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
        ui.displayHitOrMiss(hit);

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

    private static List<String> readLogFile(Path logPath) {
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
        return lines;
    }

}
