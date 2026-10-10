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
	 * The name of a {@code boolean} parameter of this method that makes a call a dry run.
	 *
	 * <p>
	 * A call with that parameter set to {@code true} changes nothing, so it runs as a read whatever
	 * {@link #mode()} declares: without an approval prompt and outside the transaction of the turn.
	 * The method has to keep that promise, and it is a promise about security: whatever a dry run
	 * changed would be changed without the user being asked.
	 * </p>
	 *
	 * <p>
	 * Empty, the default, means the tool has no dry run.
	 * </p>
	 *
	 * @see Tool#mode(java.util.Map)
	 */
	String dryRun() default "";

	/**
	 * The name of a method of the same class that refuses a call before it runs.
	 *
	 * <p>
	 * That method takes exactly the parameters of the tool method, receives the same arguments and
	 * returns the refusal, phrased for the model, or <code>null</code>. It is asked before anybody is
	 * asked to approve the call, so it must not change anything. The tool method has to refuse the
	 * same calls itself: not every caller asks the check first.
	 * </p>
	 *
	 * <p>
	 * Empty, the default, means the tool refuses nothing early.
	 * </p>
	 *
	 * @see Tool#refusal(java.util.Map, ToolExecutionContext)
	 */
	String check() default "";

	/**
	 * The name of a method of the same class that says why this tool can do nothing in the current
	 * run.
	 *
	 * <p>
	 * That method takes exactly one parameter, the {@link ToolExecutionContext} of the run, and returns
	 * the reason, phrased for the model, or <code>null</code> when the tool can act. A tool that answers
	 * is not offered to the model, and a call that names it anyway is answered with the reason. The
	 * method must not change anything.
	 * </p>
	 *
	 * <p>
	 * Empty, the default, means the tool can act in every run.
	 * </p>
	 *
	 * @see Tool#unavailableReason(ToolExecutionContext)
	 */
	String unavailable() default "";

	/**
	 * Who marks this tool's answer as untrusted content.
	 *
	 * <p>
	 * Defaults to {@link AnswerMarking#FRAMEWORK}, which wraps the answer in the
	 * {@code MODEL_DATA} envelope - the same marking a TL-Script tool's answer gets, so that moving a
	 * tool from TL-Script to Java does not silently take the envelope off (issue #478).
	 * Declare {@link AnswerMarking#NONE} when the answer is this application's own sentence, and
	 * {@link AnswerMarking#TOOL} when the tool marks its content itself.
	 * </p>
	 *
	 * @see AnswerMarking
	 * @see UntrustedEnvelope
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

	/**
	 * The qualified names of the model types this tool works on, e.g. {@code "myapp:Customer"}.
	 *
	 * <p>
	 * <b>Enforced.</b> Before a declared tool runs, the acting principal's READ rights are asked for
	 * every declared type. A tool whose declared types the principal may read none of is refused
	 * before it runs - with a reason naming the type - and is left out of the tool schemas the model
	 * is sent, so it does not plan around a tool it could not use. A tool that declares nothing is
	 * unaffected: an empty declaration is "this tool makes no claim", never "this tool touches
	 * nothing".
	 * </p>
	 *
	 * <p>
	 * What it buys, and in this order: the model gets "you have no read access to Customer" instead
	 * of an empty result it reports to the user as "there are no customers"; the request carries one
	 * tool less; and a reviewer can read off which tools claim which types.
	 * </p>
	 *
	 * <p>
	 * <b>Declare a type only where its READ right is granted to a role held on the security root.</b>
	 * The check is an L1 check and reads roles there, like every other one - see
	 * {@code doc/security/agent-security-architecture.md}, section 3.3.4. A type whose read right is
	 * derived <em>per object</em> instead, as {@code tl.ai.screenshots:Screenshot} derives
	 * {@code AiBinaryOwner} from the screenshot's own owner, must <b>not</b> be declared: no account
	 * holds such a role on the root, so declaring it would refuse the owner their own object. Where
	 * you cannot tell, declare nothing - the per-object checks below are what protects the data
	 * either way.
	 * </p>
	 *
	 * <p>
	 * <b>Not every tool can declare, and none has to.</b> Instance browsing, TL-Script execution and
	 * search take the type from their arguments, so there is nothing fixed to name; they are governed
	 * by the per-object checks that apply to every tool regardless - {@code ModelSecurity.canRead} on
	 * each object touched, the read filtering of the results, and the pre-commit guard. A declaration
	 * is an early, coarse refusal in front of those checks, never a replacement for them, and a tool
	 * can lie about what it touches while the per-object checks cannot be lied to.
	 * </p>
	 *
	 * <p>
	 * A name that resolves to no type of the application model is <em>skipped</em>, with a warning:
	 * a typo is a broken declaration and not a statement about rights, and turning it into a denial
	 * would take a working tool away from every user for nothing. A tool all of whose declarations
	 * are unresolvable is treated as declaring nothing.
	 * </p>
	 *
	 * @see Tool#types()
	 */
	String[] types() default {};

}
