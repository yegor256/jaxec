/*
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2023-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
