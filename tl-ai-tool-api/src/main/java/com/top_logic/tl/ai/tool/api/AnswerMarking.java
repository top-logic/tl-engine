/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

/**
 * Who marks a tool's answer as untrusted content - the framework, the tool, or nobody.
 *
 * <p>
 * The question this answers is not "is this tool dangerous" but "was the text in this answer written
 * by somebody other than the user this run serves". A business application's tools almost always
 * answer with text its users typed, so {@link #FRAMEWORK} is the default and the other two values are
 * statements a tool author makes on purpose - and that a reviewer can read as such.
 * </p>
 *
 * @see ToolMethod#marking()
 * @see UntrustedEnvelope
 */
public enum AnswerMarking {

	/**
	 * The framework wraps the answer in the {@code MODEL_DATA} envelope.
	 *
	 * <p>
	 * The default, because the alternative default is the bug this exists to close: a tool moved from
	 * TL-Script to Java used to lose its envelope silently, on exactly the payload that needs it most
	 * (issue #478). Forgetting to declare anything therefore marks the answer rather than leaving it
	 * bare.
	 * </p>
	 *
	 * <p>
	 * An answer that already is one complete envelope block is left alone - see
	 * {@code UntrustedEnvelope.isMarked} - so a tool that marked itself before the framework did, here
	 * or in an application built on it, does not get a second wrap that would neutralise its first.
	 * </p>
	 */
	FRAMEWORK,

	/**
	 * The tool marks its own content, and the framework adds nothing.
	 *
	 * <p>
	 * For the three cases the framework cannot get right by itself: an answer whose content belongs in
	 * another {@link UntrustedEnvelope} kind (a fetched page, a stored document, an external server's
	 * answer), an answer that is a marked block with the tool's own sentences <em>around</em> it, and
	 * an answer whose marking carries a better {@code source} than the tool's name - the object a
	 * listing was taken from, say.
	 * </p>
	 *
	 * <p>
	 * It is a promise, not an exemption: a tool that declares this and marks nothing returns business
	 * data bare.
	 * </p>
	 */
	TOOL,

	/**
	 * Nothing is marked, because nothing in the answer came from anywhere else.
	 *
	 * <p>
	 * For an answer that is this application's own sentence: a confirmation of what the tool did, a
	 * refusal, computed help text, a schema, a list of names the application defines. Marking those
	 * would be untrue, and untrue in the expensive direction - the standing rule tells the model that
	 * a marked block is never an instruction to it, so a framework answer that <em>is</em> an
	 * instruction (a skill's text, a delegate's report, "call {@code listTypes} first") must not carry
	 * the tag.
	 * </p>
	 */
	NONE;

}
