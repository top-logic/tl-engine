/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a tool that the agent can invoke.
 *
 * @see P
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
	 * Defaults to {@link ToolMode#STRUCTURAL} (fail-closed).
	 */
	ToolMode mode() default ToolMode.STRUCTURAL;

	/**
	 * Who marks this tool's answer as untrusted content.
	 *
	 * <p>
	 * Defaults to {@link AnswerMarking#FRAMEWORK}, which wraps the answer in
	 * {@link UntrustedEnvelope#MODEL_DATA} - the same marking a TL-Script tool's answer gets, so that
	 * moving a tool from TL-Script to Java does not silently take the envelope off (issue #478).
	 * Declare {@link AnswerMarking#NONE} when the answer is this application's own sentence, and
	 * {@link AnswerMarking#TOOL} when the tool marks its content itself.
	 * </p>
	 *
	 * @see AnswerMarking
	 */
	AnswerMarking marking() default AnswerMarking.FRAMEWORK;

	/**
	 * Whether this tool can safely run in parallel with other concurrency-safe tools.
	 * Defaults to {@code true}. Override to {@code false} only for tools with
	 * external concurrency constraints.
	 */
	boolean concurrencySafe() default true;

	/**
	 * Per-tool maximum character count before the agent loop truncates the result,
	 * or {@code -1} to use the global default. Set to {@link Integer#MAX_VALUE} to
	 * opt out of truncation entirely.
	 */
	int truncationHint() default -1;

	/**
	 * The tool group this tool belongs to - the unit an administrator grants to a security role.
	 *
	 * <p>
	 * Empty means ungrouped, which denies the tool to every role: with deny-by-default, a tool
	 * added without a group must not slip into an existing grant by accident. An administrator can
	 * assign a group in the access policy, which also overrides the value declared here.
	 * </p>
	 *
	 * <p>
	 * Enforced only where a deployment has configured a &lt;tool-access&gt; policy; without one,
	 * tool authorization stays unenforced and this value is inert. See
	 * {@code doc/security/agent-security-architecture.md}, section 3.2, and
	 * {@code doc/security/tool-access-configuration.md} for the administrator's view.
	 * </p>
	 *
	 * @see Tool#group()
	 */
	String group() default "";

}
