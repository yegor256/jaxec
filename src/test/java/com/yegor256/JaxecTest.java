/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Yegor Bugayenko
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

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Jaxec}.
 *
 * @since 0.1.0
 */
final class JaxecTest {

    @Test
    void runsSimpleCommand() {
        MatcherAssert.assertThat(
            new Jaxec("date")
                .with("+%Y")
                .withHome("/tmp")
                .withHome(new File("/tmp"))
                .withHome(Paths.get("/tmp"))
                .withRedirect(true)
                .exec(),
            Matchers.containsString(
                Integer.toString(
                    Calendar.getInstance().get(Calendar.YEAR)
                )
            )
        );
    }

    @Test
    void runsWithMultipleArgs() {
        MatcherAssert.assertThat(
            new Jaxec().with(Arrays.asList("date", "+%Y")).exec(),
            Matchers.containsString(
                Integer.toString(
                    Calendar.getInstance().get(Calendar.YEAR)
                )
            )
        );
    }

    @Test
    void ignoresStderr() {
        MatcherAssert.assertThat(
            new Jaxec("cat", "/file-is-absent")
                .withCheck(false)
                .withRedirect(false)
                .exec(),
            Matchers.equalTo("")
        );
    }

    @Test
    void preservesUnicode() {
        final String text = "Привет, друг!";
        MatcherAssert.assertThat(
            new Jaxec("echo", text).exec(),
            Matchers.startsWith(text)
        );
    }

    @Test
    void sendsStdinToProcess() {
        MatcherAssert.assertThat(
            new Jaxec("cat").withStdin("Hello, world!").exec(),
            Matchers.startsWith("Hello")
        );
    }

    @Test
    void sendsEmtpyStdinToProcess() {
        MatcherAssert.assertThat(
            new Jaxec("cat").withStdin(new byte[] {}).exec(),
            Matchers.equalTo("")
        );
    }

    @Test
    void runsInvalidCommand() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Jaxec("/tmp/this-command-doesnt-exist")
                .withCheck(true)
                .exec()
        );
    }

    @Test
    void catchesErrorCode() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Jaxec("cat", "/the-file-is-absent.txt").exec()
        );
    }

}
