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

/**
 * There is only one class {@link com.yegor256.Jaxec} that helps
 * you execute a shell command through a simple fluent interface.
 *
 * <p>It is as simple as the following:</p>
 *
 * <code><pre> String stdout = new Jaxec("ls", "-al", "/tmp")
 *   .withHome("/home/me") // run it in this directory
 *   .withRedirect(false) // don't redirect STDERR to STDOUT
 *   .exec();</pre></code>
 *
 * <p>The class {@link com.yegor256.Jaxec} is immutable, meaning that
 * on every <code>with()</code> call you get a new instance
 * of the class.</p>
 *
 * <p>The output of the shell command is sent to
 * <a href="https://www.slf4j.org/">Slf4j logging facility</a>,
 * which you can redirect to Log4j or any other
 * logging engine. Log events are sent to the
 * <code>com.jcabi.log</code> package.</p>
 *
 * @since 0.0.1
 */
package com.yegor256;
