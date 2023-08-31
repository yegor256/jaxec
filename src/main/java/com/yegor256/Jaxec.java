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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
 * <p>The default home directory is the one defined
 * in <code>"user.dir"</code>. You can change this via
 * the {@link Jaxec#withHome(File)} method.</p>
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
 * logging engine.</p>
 *
 * @since 0.0.1
 */
public final class Jaxec {

    /**
     * Arguments.
     */
    private final Collection<String> arguments;

    /**
     * Home directory.
     */
    private final File home;

    /**
     * Redirect STDERR to STDOUT?
     */
    private final boolean redirect;

    /**
     * Check exit code and fail if it's not zero?
     */
    private final boolean check;

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
        this(args, dir, true);
    }

    /**
     * Ctor.
     * @param args The command line arguments
     * @param dir Home directory
     * @param redir Redirect STDERR to STDOUT?
     */
    public Jaxec(final Collection<String> args, final File dir,
        final boolean redir) {
        this(args, dir, redir, true);
    }

    /**
     * Ctor.
     * @param args The command line arguments
     * @param dir Home directory
     * @param redir Redirect STDERR to STDOUT?
     * @param chck Check exit code and fail if it's not zero?
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Jaxec(final Collection<String> args, final File dir,
        final boolean redir, final boolean chck) {
        this.arguments = Collections.unmodifiableCollection(args);
        this.home = dir;
        this.redirect = redir;
        this.check = chck;
    }

    /**
     * With these arguments too.
     * @param args The arguments to append
     * @return New Jaxec with these new arguments
     */
    public Jaxec with(final String... args) {
        return this.with(Arrays.asList(args));
    }

    /**
     * With these arguments too.
     * @param args The arguments to append
     * @return New Jaxec with a new argument
     */
    public Jaxec with(final Iterable<String> args) {
        final Collection<String> extra = new LinkedList<>();
        extra.addAll(this.arguments);
        for (final String arg : args) {
            extra.add(arg);
        }
        return new Jaxec(extra, this.home, this.redirect, this.check);
    }

    /**
     * With checking?
     * @param chck If it's TRUE, the exit code of the shell command will be checked
     *  and an exception will be thrown if it's not zero
     * @return New Jaxec with a new checking mechanism
     */
    public Jaxec withCheck(final boolean chck) {
        return new Jaxec(this.arguments, this.home, this.redirect, chck);
    }

    /**
     * With home directory.
     * @param dir Home directory
     * @return New Jaxec with a new home directory
     */
    public Jaxec withHome(final Path dir) {
        return this.withHome(dir.toFile());
    }

    /**
     * With home directory.
     * @param dir Home directory
     * @return New Jaxec with a new home directory
     */
    public Jaxec withHome(final File dir) {
        return new Jaxec(this.arguments, dir, this.redirect, this.check);
    }

    /**
     * With home directory.
     * @param dir Home directory
     * @return New Jaxec with a new home directory
     */
    public Jaxec withHome(final String dir) {
        return this.withHome(new File(dir));
    }

    /**
     * Redirect STDERR to STDOUT?
     * @param redir TRUE if redirect is necessary
     * @return New Jaxec with a new redirecting status
     */
    public Jaxec withRedirect(final boolean redir) {
        return new Jaxec(this.arguments, this.home, redir, this.check);
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
        final List<String> args = new LinkedList<>();
        args.addAll(this.arguments);
        final Process proc = new ProcessBuilder()
            .command(args)
            .directory(this.home)
            .redirectErrorStream(true)
            .start();
        final String stdout;
        try (VerboseProcess vproc = new VerboseProcess(proc)) {
            if (this.check) {
                stdout = vproc.stdout();
            } else {
                stdout = vproc.stdoutQuietly();
            }
        }
        return stdout;
    }
}
