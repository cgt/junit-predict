package net.gamrath.junitpredict;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

class SwingUI implements UI {
    @Override
    public Prediction promptForPrediction() {
        final var prediction = new AtomicReference<Prediction>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                final var choice = JOptionPane.showOptionDialog(
                        null,
                        "Do you predict that ALL tests will PASS or that ANY will FAIL?",
                        "Call your shot!",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new Object[]{"FAIL", "PASS"},
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
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return prediction.get();
    }

    @Override
    public void displayHitOrMiss(boolean hit) {
        try {
            SwingUtilities.invokeAndWait(() ->
                    JOptionPane.showMessageDialog(
                            null,
                            hit ? "Hit" : "Miss",
                            "Prediction Result",
                            hit ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                    )
            );
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}