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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 * Simple Java shell command executor.
 *
 * <p>When you need to run a shell command:</p>
 *
 * <code><pre> String stdout = new Jaxec("ls", "-al", "/tmp")
 *   .withHome("/home/me") // run it in this directory
 *   .withRedirect(false) // don't redirect STDERR to STDOUT
 *   .exec();</pre></code>
 *
 * <p>If the exit code is not equal to zero, a runtime exception will
 * be thrown. Moreover, the STDOUT of the command will be sent to
 * the logging facility. If {@link Jaxec#withRedirect(boolean)} is set to {@code FALSE},
 * only the STDERR will be sent to the log. Be careful about what the
 * command prints to the console, since it will be visible in the
 * log in case of error.</p>
 *
 * <p>The default home directory is the one defined
 * in <code>"user.dir"</code>. You can change this via
 * the {@link Jaxec#withHome(File)} method (and its overloaded siblings).</p>
 *
 * <p>By default, STDERR is redirected to STDOUT. You can change
 * this by using the {@link Jaxec#withRedirect(boolean)} method.</p>
 *
 * <p>Objects of this class are immutable, meaning that
 * on every call to one of <code>with()</code> methods you
 * get a new instance of the class.</p>
 *
 * <p>The output of the shell command is sent to
 * <a href="https://www.slf4j.org/">Slf4j logging facility</a>,
 * which you can redirect to Log4j or any other
 * logging engine. Log events are sent to the
 * <code>com.jcabi.log.VerboseProcess</code> class.
 * The level for stdout is `DEBUG`, while the level
 * for stderr is `WARN`.</p>
 *
 * @since 0.0.1
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.GodClass" })
public final class Jaxec {

    /**
     * Arguments.
     */
    private final Collection<String> arguments;

    /**
     * The builder of the process.
     * @since 0.3.0
     */
    private final ProcessBuilder builder;

    /**
     * Check exit code and fail if it's not zero?
     */
    private final boolean check;

    /**
     * STDIN to send to the process.
     */
    private final InputStream stdin;

    /**
     * Ctor.
     * @param args The command line arguments
     */
    public Jaxec(final String... args) {
        this(Arrays.asList(args));
    }

    /**
     * Ctor.
     * @param args The command line arguments
     */
    public Jaxec(final Collection<String> args) {
        this(args, new File(System.getProperty("user.dir")));
    }

    /**
     * Ctor.
     * @param args The command line arguments
     * @param dir Home directory
     */
    public Jaxec(final Collection<String> args, final File dir) {
        this(args, dir, true, new ByteArrayInputStream(new byte[] {}));
    }

    /**
     * Ctor.
     * @param args The command line arguments
     * @param dir Home directory
     * @param chck Check exit code and fail if it's not zero?
     * @param input STDIN
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Jaxec(final Collection<String> args, final File dir,
        final boolean chck, final InputStream input) {
        this(new ProcessBuilder().directory(dir), args, chck, input);
    }

    /**
     * Ctor.
     * @param pcs Process builder
     * @param args The command line arguments
     * @param chck Check exit code and fail if it's not zero?
     * @param input STDIN
     * @since 0.3.0
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Jaxec(final ProcessBuilder pcs, final Collection<String> args,
        final boolean chck, final InputStream input) {
        this.builder = pcs;
        this.arguments = Collections.unmodifiableCollection(args);
        this.check = chck;
        this.stdin = input;
    }

    /**
     * With these arguments too.
     * @param args The arguments to append
     * @return New Jaxec with these new arguments
     */
    public Jaxec with(final String... args) {
        if (args == null) {
            throw new IllegalArgumentException("The list of arguments can't be NULL");
        }
        return this.with(Arrays.asList(args));
    }

    /**
     * With these arguments too.
     * @param args The arguments to append
     * @return New Jaxec with a new argument
     */
    public Jaxec with(final Iterable<String> args) {
        if (args == null) {
            throw new IllegalArgumentException("The list of arguments can't be NULL");
        }
        final Collection<String> extra = new LinkedList<>(this.arguments);
        int pos = 0;
        for (final String arg : args) {
            pos += 1;
            if (arg == null) {
                throw new IllegalArgumentException(
                    String.format("The argument no.%d can't be NULL", pos)
                );
            }
            extra.add(arg);
        }
        return new Jaxec(this.builder, extra, this.check, this.stdin);
    }

    /**
     * With checking?
     * @param chck If it's TRUE, the exit code of the shell command will be checked
     *  and an exception will be thrown if it's not zero
     * @return New Jaxec with a new checking mechanism
     */
    public Jaxec withCheck(final boolean chck) {
        return new Jaxec(this.builder, this.arguments, chck, this.stdin);
    }

    /**
     * With home directory.
     * @param dir Home directory
     * @return New Jaxec with a new home directory
     */
    public Jaxec withHome(final Path dir) {
        if (dir == null) {
            throw new IllegalArgumentException("The HOME can't be NULL");
        }
        return this.withHome(dir.toFile());
    }

    /**
     * With home directory.
     * @param dir Home directory
     * @return New Jaxec with a new home directory
     */
    public Jaxec withHome(final File dir) {
        if (dir == null) {
            throw new IllegalArgumentException("The HOME can't be NULL");
        }
        return new Jaxec(this.arguments, dir, this.check, this.stdin);
    }

    /**
     * With home directory.
     * @param dir Home directory
     * @return New Jaxec with a new home directory
     */
    public Jaxec withHome(final String dir) {
        if (dir == null) {
            throw new IllegalArgumentException("The HOME can't be NULL");
        }
        return this.withHome(new File(dir));
    }

    /**
     * Redirect STDERR to STDOUT?
     * @param redir TRUE if redirect is necessary
     * @return New Jaxec with a new redirecting status
     */
    public Jaxec withRedirect(final boolean redir) {
        return new Jaxec(
            this.builder.redirectErrorStream(redir),
            this.arguments, this.check, this.stdin
        );
    }

    /**
     * Redirect STDOUT to this file.
     * @param pipe The destination to redirect to
     * @return New Jaxec with a new redirecting status
     * @since 0.3.0
     */
    public Jaxec withStdout(final ProcessBuilder.Redirect pipe) {
        return new Jaxec(
            this.builder.redirectOutput(pipe),
            this.arguments, this.check, this.stdin
        );
    }

    /**
     * Redirect STDOUT to this file.
     * @param pipe The destination to redirect to
     * @return New Jaxec with a new redirecting status
     * @since 0.3.0
     */
    public Jaxec withStderr(final ProcessBuilder.Redirect pipe) {
        return new Jaxec(
            this.builder.redirectError(pipe),
            this.arguments, this.check, this.stdin
        );
    }

    /**
     * The STDIN to send to the process.
     * @param input STDIN text
     * @return New Jaxec with a new STDIN
     */
    public Jaxec withStdin(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("The STDIN can't be NULL");
        }
        return this.withStdin(
            new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8)
            )
        );
    }

    /**
     * The STDIN to send to the process.
     * @param bytes STDIN to send to the process
     * @return New Jaxec with a new STDIN
     */
    public Jaxec withStdin(final byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("The STDIN can't be NULL");
        }
        return this.withStdin(
            new ByteArrayInputStream(bytes)
        );
    }

    /**
     * The STDIN to send to the process.
     * @param input STDIN text
     * @return New Jaxec with a new STDIN
     */
    public Jaxec withStdin(final InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("The STDIN can't be NULL");
        }
        return new Jaxec(this.builder, this.arguments, this.check, input);
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
        Logger.debug(this, "+%s", String.join(" ", this.arguments));
        final Process proc = this.builder
            .command(new LinkedList<>(this.arguments))
            .start();
        try (OutputStream stream = proc.getOutputStream()) {
            final byte[] buffer = new byte[1024];
            while (true) {
                final int len = this.stdin.read(buffer);
                if (len < 0) {
                    break;
                }
                stream.write(buffer, 0, len);
            }
        }
        final String stdout;
        try (VerboseProcess vproc = new VerboseProcess(proc, Level.FINE, Level.WARNING)) {
            final VerboseProcess.Result result;
            try {
                result = vproc.waitFor();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            if (this.check && result.code() != 0) {
                Logger.error(this, result.stderr());
                throw new IllegalArgumentException(
                    String.format(
                        "Non-zero exit code #%d of '%s'",
                        result.code(), this.arguments.iterator().next()
                    )
                );
            }
            stdout = result.stdout();
        }
        return stdout;
    }
}
