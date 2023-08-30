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

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseProcess;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.IoChecked;
import org.cactoos.scalar.LengthOf;

/**
 * Java command line executor.
 *
 * @since 0.1.0
 */
public final class Jaxec {

    /**
     * Arguments.
     */
    private final List<String> arguments;

    /**
     * Ctor.
     * @param args The command line arguments
     */
    public Jaxec(final String... args) {
        this.arguments = Arrays.asList(args);
    }

    /**
     * Execute it and return the output.
     * @return Stdout and stderr together
     */
    public String exec() {
        try {
            return this.execUnsafe();
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Execute it and return the output.
     * @return Stdout and stderr together
     * @throws IOException If fails
     */
    public String execUnsafe() throws IOException {
        Logger.debug(this, "+%s", this.arguments);
        final Process proc = new ProcessBuilder()
            .command(this.arguments)
            .directory(new File(System.getProperty("user.dir")))
            .redirectErrorStream(true)
            .start();
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (VerboseProcess vproc = new VerboseProcess(proc)) {
            new IoChecked<>(
                new LengthOf(
                    new TeeInput(
                        new InputOf(vproc.stdoutQuietly()),
                        new OutputTo(stdout)
                    )
                )
            ).value();
        }
        return stdout.toString();
    }
}
