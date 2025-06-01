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

    @Override
    public void afterAll(ExtensionContext context) {
        final var hit = prediction.test(resultByTestName.values());
        ui.displayHitOrMiss(hit);

        final var testClass = context.getRequiredTestClass().getCanonicalName();
        final var logPath = Path.of("predictions-%s.csv".formatted(testClass));

        final var lines = readLogFile(logPath);
        final var newLines = Logger.createLog(lines, hit, this.prediction);
        writeLogFile(logPath, newLines);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static List<String> readLogFile(Path logPath) {
        try {
            return Files
                    .readAllLines(logPath, StandardCharsets.UTF_8)
                    .stream()
                    .filter(line -> !line.startsWith("STATS:"))
                    .toList();
        } catch (NoSuchFileException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void writeLogFile(Path logPath, ArrayList<String> newLines) {
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
