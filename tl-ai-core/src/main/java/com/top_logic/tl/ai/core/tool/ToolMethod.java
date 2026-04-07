/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a tool that the agent can invoke.
 *
 * <p>The annotated method is discovered by {@link AnnotatedTools#from(Object)} and
 * wrapped as a {@link Tool} instance. The method's parameters must be annotated
 * with {@link P} to provide descriptions for the LLM.</p>
 *
 * <p><b>Thread safety:</b> The provider object (the instance passed to
 * {@link AnnotatedTools#from(Object)}) is shared across all users when the
 * {@link ToolRegistry} is shared. Annotated methods <em>must not</em> rely on
 * mutable instance fields. Per-user state should be received via a
 * {@link com.top_logic.tl.ai.core.agent.ToolContext} parameter, not stored on
 * the provider object.</p>
 *
 * @see P
 * @see AnnotatedTools
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolMethod {

	/**
	 * Description of what this tool does. Sent to the LLM.
	 * Multiple values are joined with newline.
	 */
	String[] description();

	/**
	 * Tool name. Defaults to the Java method name if empty.
	 */
	String name() default "";

	/**
	 * What kind of state change this tool makes.
	 * Defaults to {@link ToolMode#GLOBAL} (fail-closed).
	 */
	ToolMode mode() default ToolMode.GLOBAL;

	/**
	 * Whether this tool can safely run in parallel with other concurrency-safe tools.
	 * Defaults to {@code false} (fail-closed).
	 */
	boolean concurrencySafe() default false;

}
