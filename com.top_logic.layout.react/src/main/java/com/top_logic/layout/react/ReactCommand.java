/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method on a {@link ReactControl} subclass as a command handler for a {@code React}
 * client command.
 *
 * <p>
 * Annotated methods may declare any subset of these parameters, in the order shown:
 * </p>
 * <ol>
 * <li>{@link ViewDisplayContext} - the control's stored view context</li>
 * <li>{@code Map<String, Object>} - the raw arguments from the client</li>
 * </ol>
 *
 * <p>
 * No parameters is also valid. The return type must be either
 * {@link com.top_logic.tool.boundsec.HandlerResult} or {@code void}. A {@code void} method
 * implicitly returns {@link com.top_logic.tool.boundsec.HandlerResult#DEFAULT_RESULT}.
 * </p>
 *
 * <p>
 * Methods may have any visibility (including {@code private} and package-private). Resolution
 * happens lazily on first instantiation of each {@link ReactControl} subclass and is cached per
 * class. The hot path uses {@link java.lang.invoke.MethodHandle} for zero-reflection dispatch.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReactCommand {

	/**
	 * The command identifier sent by the {@code React} client (e.g. {@code "click"}, {@code "sort"},
	 * {@code "select"}).
	 */
	String value();
}
