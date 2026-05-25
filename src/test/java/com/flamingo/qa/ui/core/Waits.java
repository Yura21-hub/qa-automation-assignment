package com.flamingo.qa.ui.core;

import java.time.Duration;
import java.util.function.BooleanSupplier;

public final class Waits {
    private static final Duration POLL_INTERVAL = Duration.ofMillis(100);

    private Waits() {
    }

    public static void until(BooleanSupplier condition, Duration timeout, String description) {
        long deadline = System.nanoTime() + timeout.toNanos();
        RuntimeException lastRuntimeException = null;

        while (System.nanoTime() < deadline) {
            try {
                if (condition.getAsBoolean()) {
                    return;
                }
            } catch (RuntimeException exception) {
                lastRuntimeException = exception;
            }

            sleep();
        }

        AssertionError timeoutError = new AssertionError(
                "Timed out waiting for " + description + " within " + timeout.toMillis() + " ms"
        );
        if (lastRuntimeException != null) {
            timeoutError.addSuppressed(lastRuntimeException);
        }
        throw timeoutError;
    }

    private static void sleep() {
        try {
            Thread.sleep(POLL_INTERVAL.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting", exception);
        }
    }
}
