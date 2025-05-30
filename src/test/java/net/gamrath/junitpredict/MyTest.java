package net.gamrath.junitpredict;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(TestPredictionExtension.class)
@Disabled("For demonstration")
class MyTest {
    @Test
    void passes() {
    }

    @Test
    void fails() {
        fail();
    }

    @Test
    void errors() {
        throw new RuntimeException("not an assertion error");
    }
}