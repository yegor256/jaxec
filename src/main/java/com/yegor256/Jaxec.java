/*
 * SPDX-FileCopyrightText: Copyright (c) 2023-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

/**
 * Simple Java shell command executor.
 *
 * <p>When you need to run a shell command:</p>
 *
 * <code><pre> String stdout = new Jaxec("ls", "-al", "/tmp")
 *   .withHome("/home/me") // run it in this directory
 *   .withRedirect(false) // don't redirect STDERR to STDOUT
 *   .exec()
 *   .stdout();</pre></code>
 *
 * <p>If the exit code is not equal to zero, a runtime exception will
 * be thrown. Moreover, the STDOUT of the command will be sent to
 * the logging facility. If {@link Jaxec#withRedirect(boolean)} is set to {@code FALSE},
 * only the STDERR will be sent to the log. Be careful about what the
 * command prints to the console, since it will be visible in the
 * log in case of error.</p>
 *
 * <p>The default home directory is the one defined
 * in {@code "user.dir"}. You can change this via
 * the {@link Jaxec#withHome(File)} method (and its overloaded siblings).</p>
 *
 * <p>By default, STDERR is redirected to STDOUT. You can change
 * this by using the {@link Jaxec#withRedirect(boolean)} method.</p>
 *
 * <p>Objects of this class are immutable, meaning that
 * on every call to one of {@code with()} methods you
 * get a new instance of the class.</p>
 *
 * <p>The output of the shell command is sent to
 * <a href="https://www.slf4j.org/">Slf4j logging facility</a>,
 * which you can redirect to Log4j or any other
 * logging engine. Log events are sent to the
 * {@code com.jcabi.log.VerboseProcess} class.
 * The level for stdout is {@code DEBUG}, while the level
 * for stderr is {@code WARN}.</p>
 *
 * @since 0.0.1
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.GodClass" })
public final class Jaxec {

    /**
     * Command line arguments to be executed.
     */
    private final Collection<String> arguments;

    /**
     * Environment variables to pass.
     */
    private final Map<String, String> environment;

    /**
     * The builder of the process that configures the operating system process.
     * @since 0.3.0
     */
    private final ProcessBuilder builder;

    /**
     * Flag indicating whether to check exit code and fail if it's not zero.
     */
    private final boolean check;

    /**
     * Input stream to send to the process as STDIN.
     */
    private final InputStream stdin;

    /**
     * Constructs a new Jaxec with the given command line arguments.
     * @param args The command line arguments (first element is the command, rest are parameters)
     */
    public Jaxec(final String... args) {
        this(Arrays.asList(args));
    }

    /**
     * Constructs a new Jaxec with the given command line arguments.
     * Uses the current working directory as the home directory.
     * @param args The command line arguments as a collection
     */
    public Jaxec(final Collection<String> args) {
        this(args, new File(System.getProperty("user.dir")));
    }

    /**
     * Constructs a new Jaxec with the given arguments and home directory.
     * Exit code checking is enabled by default.
     * @param args The command line arguments
     * @param dir Home directory where the command will be executed
     */
    public Jaxec(final Collection<String> args, final File dir) {
        this(args, dir, true, new ByteArrayInputStream(new byte[] {}));
    }

    /**
     * Constructs a new Jaxec with full configuration.
     * @param args The command line arguments
     * @param dir Home directory where the command will be executed
     * @param chk Check exit code and fail if it's not zero
     * @param input Input stream to be used as STDIN for the process
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Jaxec(final Collection<String> args, final File dir,
        final boolean chk, final InputStream input) {
        this(new ProcessBuilder().directory(dir), args, chk, input);
    }

    /**
     * Constructs a new Jaxec with a custom process builder.
     * @param pcs Process builder with pre-configured settings
     * @param args The command line arguments
     * @param chk Check exit code and fail if it's not zero
     * @param input Input stream to be used as STDIN for the process
     * @since 0.3.0
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Jaxec(final ProcessBuilder pcs, final Collection<String> args,
        final boolean chk, final InputStream input) {
        this(pcs, args, chk, input, Collections.emptyMap());
    }

    /**
     * Constructs a new Jaxec with a custom process builder.
     * @param pcs Process builder with pre-configured settings
     * @param args The command line arguments
     * @param chk Check exit code and fail if it's not zero
     * @param input Input stream to be used as STDIN for the process
     * @param env Environment variables to set for the process
     * @since 0.5.0
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Jaxec(final ProcessBuilder pcs, final Collection<String> args,
        final boolean chk, final InputStream input, final Map<String, String> env) {
        this.builder = pcs;
        this.arguments = Collections.unmodifiableCollection(args);
        this.check = chk;
        this.stdin = input;
        this.environment = Collections.unmodifiableMap(env);
    }

    /**
     * Appends additional arguments to the command.
     * @param args The arguments to append to the existing command line
     * @return New Jaxec instance with the appended arguments
     */
    public Jaxec with(final String... args) {
        if (args == null) {
            throw new IllegalArgumentException("The list of arguments can't be NULL");
        }
        return this.with(Arrays.asList(args));
    }

    /**
     * Appends additional arguments to the command.
     * @param args The arguments to append as an iterable collection
     * @return New Jaxec instance with the appended arguments
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
        return new Jaxec(this.builder, extra, this.check, this.stdin, this.environment);
    }

    /**
     * Configures whether to check the exit code of the executed command.
     * @param chk If true, the exit code of the shell command will be checked
     *  and an exception will be thrown if it's not zero
     * @return New Jaxec instance with the specified checking behavior
     */
    public Jaxec withCheck(final boolean chk) {
        return new Jaxec(this.builder, this.arguments, chk, this.stdin, this.environment);
    }

    /**
     * Sets the working directory for command execution.
     * @param dir Home directory as a Path object
     * @return New Jaxec instance with the specified home directory
     */
    public Jaxec withHome(final Path dir) {
        if (dir == null) {
            throw new IllegalArgumentException("The HOME can't be NULL");
        }
        return this.withHome(dir.toFile());
    }

    /**
     * Sets the working directory for command execution.
     * @param dir Home directory as a File object
     * @return New Jaxec instance with the specified home directory
     */
    public Jaxec withHome(final File dir) {
        if (dir == null) {
            throw new IllegalArgumentException("The HOME can't be NULL");
        }
        return new Jaxec(this.arguments, dir, this.check, this.stdin);
    }

    /**
     * Sets the working directory for command execution.
     * @param dir Home directory as a String path
     * @return New Jaxec instance with the specified home directory
     */
    public Jaxec withHome(final String dir) {
        if (dir == null) {
            throw new IllegalArgumentException("The HOME can't be NULL");
        }
        return this.withHome(new File(dir));
    }

    /**
     * Configures whether to redirect STDERR to STDOUT.
     * @param redir True to merge STDERR with STDOUT, false to keep them separate
     * @return New Jaxec instance with the specified redirection setting
     */
    public Jaxec withRedirect(final boolean redir) {
        return new Jaxec(
            this.builder.redirectErrorStream(redir),
            this.arguments, this.check, this.stdin,
            this.environment
        );
    }

    /**
     * Redirects STDOUT to a specified destination.
     * @param pipe The redirect destination (e.g., file, pipe, or discard)
     * @return New Jaxec instance with STDOUT redirected
     * @since 0.3.0
     */
    public Jaxec withStdout(final ProcessBuilder.Redirect pipe) {
        return new Jaxec(
            this.builder.redirectOutput(pipe),
            this.arguments, this.check, this.stdin,
            this.environment
        );
    }

    /**
     * Redirects STDERR to a specified destination.
     * @param pipe The redirect destination (e.g., file, pipe, or discard)
     * @return New Jaxec instance with STDERR redirected
     * @since 0.3.0
     */
    public Jaxec withStderr(final ProcessBuilder.Redirect pipe) {
        return new Jaxec(
            this.builder.redirectError(pipe),
            this.arguments, this.check, this.stdin,
            this.environment
        );
    }

    /**
     * Sets the STDIN content for the process from a string.
     * @param input Text to send to the process's STDIN
     * @return New Jaxec instance with the specified STDIN content
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
     * Sets the STDIN content for the process from a byte array.
     * @param bytes Binary data to send to the process's STDIN
     * @return New Jaxec instance with the specified STDIN content
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
     * Sets the STDIN content for the process from an input stream.
     * @param input Input stream to be piped to the process's STDIN
     * @return New Jaxec instance with the specified STDIN content
     */
    public Jaxec withStdin(final InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("The STDIN can't be NULL");
        }
        return new Jaxec(this.builder, this.arguments, this.check, input, this.environment);
    }

    /**
     * With this new environment variable.
     * @param name The name of the variable
     * @param value The value of it
     * @return New Jaxec instance with the specified STDIN content
     * @since 0.5.0
     */
    public Jaxec withEnv(final String name, final String value) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                "The name of the env variable can't be NULL or empty"
            );
        }
        if (value == null) {
            throw new IllegalArgumentException(
                "The value of the env variable can't be NULL"
            );
        }
        final Map<String, String> env = new HashMap<>(this.environment);
        env.put(name, value);
        return new Jaxec(this.builder, this.arguments, this.check, this.stdin, env);
    }

    /**
     * Executes the command and returns the result.
     * Throws a runtime exception if the command fails.
     * @return Result object containing exit code, stdout, and stderr
     */
    public Result exec() {
        try {
            return this.execUnsafe();
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Executes the command and returns the result.
     * This method may throw a checked IOException.
     * @return Result object containing exit code, stdout, and stderr
     * @throws IOException If the process cannot be started or I/O error occurs
     */
    public Result execUnsafe() throws IOException {
        Logger.debug(this, "+%s", String.join(" ", this.arguments));
        final ProcessBuilder bdr = this.builder.command(new LinkedList<>(this.arguments));
        bdr.environment().putAll(this.environment);
        final Process proc = bdr.start();
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
        final VerboseProcess.Result result;
        try (VerboseProcess vproc = new VerboseProcess(proc, Level.INFO, Level.WARNING)) {
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
        }
        return new Result() {
            @Override
            public int code() {
                return result.code();
            }

            @Override
            public String stdout() {
                return result.stdout();
            }

            @Override
            public String stderr() {
                return result.stderr();
            }
        };
    }

}
