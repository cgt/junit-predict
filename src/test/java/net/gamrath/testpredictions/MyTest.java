package net.gamrath.testpredictions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(TestPredictionExtension.class)
class MyTest {
    @Test
    void passes() {
    }

    @Test
    void fails() {
        fail();
    }
}