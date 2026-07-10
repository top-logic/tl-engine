/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.layout.react.ReactContext;

/**
 * Marks a method on a {@link ReactControl} subclass as a command handler for a {@code React}
 * client command.
 *
 * <p>
 * Annotated methods may declare any subset of these parameters, in the order shown:
 * </p>
 * <ol>
 * <li>{@link ReactContext} - the control's stored view context</li>
 * <li>the command arguments, as <em>either</em> a raw {@code Map<String, Object>} from the client,
 * <em>or</em> a {@link ReactCommand} subtype the client JSON binds into (typed getters,
 * an advertised JSON schema, and a recorded-step rendering all derived from the one interface)</li>
 * </ol>
 *
 * <p>
 * No parameters is also valid. The return type must be either
 * {@link com.top_logic.tool.boundsec.HandlerResult} or {@code void}. A {@code void} method
 * implicitly returns {@code HandlerResult.DEFAULT_RESULT}.
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
public @interface ReactCommandHandler {

	/**
	 * The command identifier sent by the {@code React} client (e.g. {@code "click"}, {@code "sort"},
	 * {@code "select"}).
	 */
	String value();

	/**
	 * The arguments this command accepts, advertised to the headless agent interface so a consumer
	 * need not guess the shape of the {@code arguments} map. Empty for argument-less commands.
	 */
	ReactParam[] params() default {};

	/**
	 * Whether this is a technical/chrome command — a UI-incidental gesture (closing a transient
	 * notification, toggling collapse/maximize, popping out a panel) rather than meaningful user
	 * intent.
	 *
	 * <p>
	 * A technical command is omitted from the headless agent's advertised actions <em>and</em> never
	 * captured by the script recorder. Declaring it here, on the command, keeps the classification
	 * co-located with the handler so it cannot drift out of a separately-maintained set.
	 * </p>
	 */
	boolean technical() default false;
}
