/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023-2025 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.yegor256;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
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
    @DisabledOnOs(OS.WINDOWS)
    void catchesStderr() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("cat", "/file-is-definitely-absent")
                .withCheck(false)
                .exec()
                .stdout(),
            Matchers.containsString("No such file or directory")
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
    @DisabledOnOs(OS.WINDOWS)
    void sendsStdinToProcess() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("cat").withStdin("Hello, world!").exec().stdout(),
            Matchers.startsWith("Hello")
        );
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void sendsEmtpyStdinToProcess() {
        MatcherAssert.assertThat(
            "must work just fine",
            new Jaxec("cat").withStdin(new byte[] {}).exec().stdout(),
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

}
