package net.gamrath.junitpredict;

import java.util.ArrayList;
import java.util.List;

class Logger {
    static ArrayList<String> createLog(List<String> lines, boolean hit, final Prediction prediction) {
        var hits = 0;
        var misses = 0;
        for (final var line : lines) {
            final var parts = line.split(",");
            final var isHit = Boolean.parseBoolean(parts[1]);
            if (isHit) {
                hits++;
            } else {
                misses++;
            }
        }
        if (hit) {
            hits++;
        } else {
            misses++;
        }

        final var log = formatLogLine(prediction, hit);
        final var newLines = new ArrayList<>(lines);
        newLines.add(log);
        newLines.add(formatStatsLine(hits, misses));
        return newLines;
    }

    private static String formatLogLine(final Prediction prediction, boolean hit) {
        return "%s,%s".formatted(prediction, hit);
    }

    private static String formatStatsLine(final int hits, final int misses) {
        return "STATS: hits=%d, misses=%d".formatted(hits, misses);
    }
}
