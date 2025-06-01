package net.gamrath.junitpredict;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

class SwingUI implements UI {
    @Override
    public Prediction promptForPrediction() {
        final var prediction = new AtomicReference<Prediction>();
        invokeUnchecked(() -> {
            final var options = new String[]{"FAIL", "PASS"};
            final var choice = JOptionPane.showOptionDialog(
                    null,
                    "Do you predict that ALL tests will PASS or that ANY will FAIL?",
                    "Call your shot!",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    null
            );
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
            prediction.set(result);
        });
        return Optional.ofNullable(prediction.get()).orElse(Prediction.SKIP);
    }

    @Override
    public void displayHitOrMiss(boolean hit) {
        invokeUnchecked(() ->
                JOptionPane.showMessageDialog(
                        null,
                        hit ? "Hit" : "Miss",
                        "Prediction Result",
                        hit ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                )
        );
    }

    private static void invokeUnchecked(Runnable r) {
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            // headless
            if (e.getCause() instanceof HeadlessException) {
                // do nothing
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}