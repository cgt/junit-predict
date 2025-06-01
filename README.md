# junit-predict

A JUnit 5 extension for teaching predictive test-driven development (TDD).

## Installation

```xml

<dependency>
    <groupId>net.gamrath</groupId>
    <artifactId>junit-predict</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Example usage

Annotate your test class with `@ExtendWith(Predict.class)`:

```java
import net.gamrath.junitpredict.Predict;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Predict.class)
class MyTest {
    @Test
    void foo() {
        // ...
    }
}
```

When you run your tests, a dialog will prompt you to predict if all tests will pass or if any will fail.
After the tests run, you'll see a dialog showing if your prediction was correct, and results will be logged
to a CSV file in your project root.
