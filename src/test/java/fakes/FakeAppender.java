/*
 * SPDX-FileCopyrightText: Copyright (c) 2023-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package fakes;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Fake Log4j appender for testing logging functionality.
 *
 * <p>This appender captures log messages and their levels in memory,
 * allowing tests to verify that specific log messages were generated
 * at the expected log levels. It extends {@link AppenderSkeleton}
 * to integrate seamlessly with Log4j's logging infrastructure.</p>
 *
 * <p>Example usage in tests:</p>
 * <pre>{@code
 * FakeAppender appender = new FakeAppender();
 * Logger logger = Logger.getLogger("com.example.MyClass");
 * logger.addAppender(appender);
 *
 * // Execute code that logs messages
 * myObject.doSomething();
 *
 * // Verify logged messages
 * assertThat(appender.getMessages(), hasItem("Expected message"));
 * assertThat(appender.getLevels(), hasItem(Level.WARN));
 *
 * // Clean up
 * logger.removeAppender(appender);
 * }</pre>
 *
 * <p>This appender is thread-safe for appending messages, but the
 * getters return copies of the internal lists to prevent external
 * modification. The appender does not require a layout since it
 * captures the raw message objects.</p>
 *
 * <p>Note: This is a test utility and should not be used in
 * production code. It stores all messages in memory and does not
 * implement any size limits or rotation policies.</p>
 *
 * @since 1.0
 * @checkstyle ProtectedMethodInFinalClassCheck (100 lines)
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class FakeAppender extends AppenderSkeleton {
    /**
     * List of captured log messages.
     * Each message is stored as a string representation of the
     * log event's message object.
     */
    private final List<String> messages = new ArrayList<>(10);

    /**
     * List of captured log levels.
     * Each level corresponds to the message at the same index
     * in the messages list.
     */
    private final List<Level> levels = new ArrayList<>(10);

    /**
     * Get all captured log messages.
     *
     * <p>Returns a defensive copy of the internal message list
     * to prevent external modification. Messages are returned
     * in the order they were logged.</p>
     *
     * @return A new list containing all captured messages
     */
    public List<String> getMessages() {
        return new ArrayList<>(this.messages);
    }

    /**
     * Get all captured log levels.
     *
     * <p>Returns a defensive copy of the internal levels list
     * to prevent external modification. Levels are returned
     * in the order they were logged and correspond to the
     * messages at the same indices.</p>
     *
     * @return A new list containing all captured log levels
     */
    public List<Level> getLevels() {
        return new ArrayList<>(this.levels);
    }

    /**
     * Clear all captured messages and levels.
     *
     * <p>This method can be useful in tests that need to
     * verify logging behavior across multiple operations
     * without interference from previous log messages.</p>
     */
    public void clear() {
        this.messages.clear();
        this.levels.clear();
    }

    /**
     * Get the number of captured messages.
     *
     * @return The count of logged messages
     */
    public int size() {
        return this.messages.size();
    }

    /**
     * Check if any messages have been captured.
     *
     * @return True if no messages have been logged,
     *  false otherwise
     */
    public boolean isEmpty() {
        return this.messages.isEmpty();
    }

    @Override
    public void close() {
        // Nothing to close - no resources to release
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(final LoggingEvent event) {
        this.messages.add(event.getMessage().toString());
        this.levels.add(event.getLevel());
    }
}
