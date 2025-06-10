/*
 * SPDX-FileCopyrightText: Copyright (c) 2023-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256;

import fakes.FakeAppender;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Jaxec}.
 *
 * @since 0.1.0
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals" })
final class JaxecTest {

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void runsSimpleCommand(@TempDir final Path dir) {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("date")
                .with("+%Y")
                .withHome(dir)
                .withRedirect(true)
                .exec()
                .stdout(),
            Matchers.containsString(
                Integer.toString(
                    Calendar.getInstance().get(Calendar.YEAR)
                )
            )
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void simpleCommandInWindows() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("help")
                .with("echo")
                .withCheck(false)
                .exec()
                .stdout(),
            Matchers.containsString("ECHO")
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void runsMaven() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("mvn")
                .with("--version")
                .exec()
                .stdout(),
            Matchers.containsString("Apache Maven")
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void runsMavenOnWindows() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("cmd")
                .with("/c")
                .with("mvn")
                .with("--version")
                .exec()
                .stdout(),
            Matchers.containsString("Apache Maven")
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void listDirectoryInWindows(@TempDir final Path temp) throws IOException {
        final Path file = temp.resolve("test.txt");
        Files.write(file, "Hello Windows".getBytes(StandardCharsets.UTF_8));
        MatcherAssert.assertThat(
            "must list files in directory",
            new Jaxec("cmd")
                .with("/c")
                .with("dir")
                .with("/b")
                .with(temp.toString())
                .exec()
                .stdout(),
            Matchers.containsString("test.txt")
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void catchesErrorCodeInWindows() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Jaxec("cmd")
                .with("/c")
                .with("type")
                .with("C:\\this-file-does-not-exist-at-all.txt")
                .exec(),
            "must throw on non-existent file"
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void runsWithMultipleArgs() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec().with(Arrays.asList("date", "+%Y")).exec().stdout(),
            Matchers.containsString(
                Integer.toString(
                    Calendar.getInstance().get(Calendar.YEAR)
                )
            )
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void runsWithMultipleArgsInWindows() {
        MatcherAssert.assertThat(
            "must work with multiple arguments",
            new Jaxec().with(Arrays.asList("cmd", "/c", "echo", "Hello", "World")).exec().stdout(),
            Matchers.containsString("Hello World")
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void ignoresStderr() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("head", "/file-is-absent")
                .withCheck(false)
                .withRedirect(false)
                .exec()
                .stdout(),
            Matchers.equalTo("")
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void ignoresStderrInWindows() {
        MatcherAssert.assertThat(
            "must ignore stderr when redirect is false",
            new Jaxec("cmd", "/c", "type C:\\file-is-not-here.txt")
                .withCheck(false)
                .withRedirect(false)
                .exec()
                .stdout(),
            Matchers.equalTo("")
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void catchesStderr() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("cat", "/file-is-definitely-absent")
                .withCheck(false)
                .exec()
                .stderr(),
            Matchers.containsString("No such file or directory")
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void catchesStderrInWindows() {
        MatcherAssert.assertThat(
            "must capture stderr",
            new Jaxec("cmd", "/c", "type C:\\file-is-absolutely-absent.txt")
                .withCheck(false)
                .exec()
                .stderr(),
            Matchers.containsString("cannot find the file")
        );
    }

    @Test
    void throwsExceptionOnWrongCommand() {
        Assertions.assertThrows(
            IOException.class,
            () -> new Jaxec("this-is-a-wrong-command")
                .withCheck(false)
                .execUnsafe(),
            "must work just fine"
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void preservesUnicode() {
        final String text = "Привет, друг!";
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("echo", text).exec().stdout(),
            Matchers.startsWith(text)
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void preservesUnicodeInWindows() {
        final String text = "Привет, друг!";
        MatcherAssert.assertThat(
            "must preserve Unicode text",
            new Jaxec("cmd", "/c", "echo", text).exec().stdout(),
            Matchers.containsString(text)
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void sendsStdinToProcess() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("cat").withStdin("Hello, world!").exec().stdout(),
            Matchers.startsWith("Hello")
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void sendsStdinToProcessInWindows() {
        MatcherAssert.assertThat(
            "must send stdin to process",
            new Jaxec("cmd", "/c", "findstr", ".*").withStdin("Hello, Windows!").exec().stdout(),
            Matchers.containsString("Hello, Windows!")
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void sendsEmptyStdinToProcess() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("cat").withStdin(new byte[] {}).exec().stdout(),
            Matchers.equalTo("")
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void sendsEmptyStdinToProcessInWindows() {
        MatcherAssert.assertThat(
            "must handle empty stdin",
            new Jaxec("cmd", "/c", "findstr", ".*")
                .withStdin(new byte[] {})
                .withCheck(false)
                .exec()
                .stdout(),
            Matchers.equalTo("")
        );
    }

    @Test
    void runsInvalidCommand() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Jaxec("/tmp/this-command-doesnt-exist")
                .withCheck(true)
                .exec(),
            "must work just fine"
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void catchesErrorCode() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Jaxec("cat", "/the-file-is-absent.txt").exec(),
            "must work just fine"
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void redirectsStdout(@TempDir final Path temp) throws IOException {
        final Path out = temp.resolve("log.txt");
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("echo")
                .with("hello")
                .withStdout(ProcessBuilder.Redirect.to(out.toFile()))
                .exec()
                .stdout(),
            Matchers.equalTo("")
        );
        MatcherAssert.assertThat(
            "must work just fine",
            new String(Files.readAllBytes(out), StandardCharsets.UTF_8),
            Matchers.equalTo("hello\n")
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void redirectsStdoutInWindows(@TempDir final Path temp) throws IOException {
        final Path out = temp.resolve("log.txt");
        MatcherAssert.assertThat(
            "must redirect stdout",
            new Jaxec("cmd")
                .with("/c")
                .with("echo")
                .with("hello")
                .withStdout(ProcessBuilder.Redirect.to(out.toFile()))
                .exec()
                .stdout(),
            Matchers.equalTo("")
        );
        MatcherAssert.assertThat(
            "must write to file",
            new String(Files.readAllBytes(out), StandardCharsets.UTF_8),
            Matchers.containsString("hello")
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void redirectsStderr(@TempDir final Path temp) throws IOException {
        final Path out = temp.resolve("errors.txt");
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("tail")
                .with("/file-is-absent")
                .withStderr(ProcessBuilder.Redirect.to(out.toFile()))
                .withCheck(false)
                .withRedirect(false)
                .exec()
                .stdout(),
            Matchers.equalTo("")
        );
        MatcherAssert.assertThat(
            "must work just fine",
            new String(Files.readAllBytes(out), StandardCharsets.UTF_8),
            Matchers.containsString("No such file or directory")
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void redirectsStderrInWindows(@TempDir final Path temp) throws IOException {
        final Path out = temp.resolve("errors.txt");
        MatcherAssert.assertThat(
            "must redirect stderr",
            new Jaxec("cmd")
                .with("/c")
                .with("type")
                .with("C:\\file-that-does-not-exist.txt")
                .withStderr(ProcessBuilder.Redirect.to(out.toFile()))
                .withCheck(false)
                .withRedirect(false)
                .exec()
                .stdout(),
            Matchers.equalTo("")
        );
        MatcherAssert.assertThat(
            "must write error to file",
            new String(Files.readAllBytes(out), StandardCharsets.UTF_8),
            Matchers.containsString("cannot find the file")
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void stderrIsLoggedToLoggingFacility() {
        final FakeAppender appender = new FakeAppender();
        final Logger logger = Logger.getLogger("com.jcabi.log.VerboseProcess");
        final Level original = logger.getLevel();
        logger.setLevel(Level.WARN);
        logger.addAppender(appender);
        try {
            new Jaxec("sh", "-c", "echo 'Error message' >&2 && exit 1")
                .withCheck(false)
                .withRedirect(false)
                .exec();
            MatcherAssert.assertThat(
                "stderr must be logged at WARN level",
                appender.getMessages(),
                Matchers.hasItem(Matchers.containsString("Error message"))
            );
            MatcherAssert.assertThat(
                "log level must be WARN",
                appender.getLevels(),
                Matchers.hasItem(Level.WARN)
            );
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(original);
        }
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void stdoutIsLoggedToLoggingFacility() {
        final FakeAppender appender = new FakeAppender();
        final Logger logger = Logger.getLogger("com.jcabi.log.VerboseProcess");
        final Level original = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(appender);
        try {
            new Jaxec("echo", "Hello from stdout")
                .withRedirect(true)
                .exec();
            MatcherAssert.assertThat(
                "stdout must be logged at DEBUG level",
                appender.getMessages(),
                Matchers.hasItem(Matchers.containsString("Hello from stdout"))
            );
            MatcherAssert.assertThat(
                "log level must be DEBUG",
                appender.getLevels(),
                Matchers.hasItem(Level.DEBUG)
            );
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(original);
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void stderrIsLoggedToLoggingFacilityInWindows() {
        final FakeAppender appender = new FakeAppender();
        final Logger logger = Logger.getLogger("com.jcabi.log.VerboseProcess");
        final Level original = logger.getLevel();
        logger.setLevel(Level.WARN);
        logger.addAppender(appender);
        try {
            new Jaxec("cmd", "/c", "echo Error message>&2 && exit 1")
                .withCheck(false)
                .withRedirect(false)
                .exec();
            MatcherAssert.assertThat(
                "stderr must be logged at WARN level",
                appender.getMessages(),
                Matchers.hasItem(Matchers.containsString("Error message"))
            );
            MatcherAssert.assertThat(
                "log level must be WARN",
                appender.getLevels(),
                Matchers.hasItem(Level.WARN)
            );
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(original);
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void stdoutIsLoggedToLoggingFacilityInWindows() {
        final FakeAppender appender = new FakeAppender();
        final Logger logger = Logger.getLogger("com.jcabi.log.VerboseProcess");
        final Level original = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(appender);
        try {
            new Jaxec("cmd", "/c", "echo Hello from Windows stdout")
                .withRedirect(true)
                .exec();
            MatcherAssert.assertThat(
                "stdout must be logged at DEBUG level",
                appender.getMessages(),
                Matchers.hasItem(Matchers.containsString("Hello from Windows stdout"))
            );
            MatcherAssert.assertThat(
                "log level must be DEBUG",
                appender.getLevels(),
                Matchers.hasItem(Level.DEBUG)
            );
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(original);
        }
    }

}
