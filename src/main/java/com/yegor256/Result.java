/*
 * SPDX-FileCopyrightText: Copyright (c) 2023-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256;

/**
 * Abstraction of process result.
 * @since 0.4
 */
public interface Result {

    /**
     * Exit code.
     * @return Exit code
     */
    int code();

    /**
     * Stdout.
     * @return Stdout
     */
    String stdout();

    /**
     * Stderr.
     * @return Stderr
     */
    String stderr();
}
