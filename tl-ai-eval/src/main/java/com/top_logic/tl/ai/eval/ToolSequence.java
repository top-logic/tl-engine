/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * An {@link Expectation} on which tools the run called: every {@link #getRequired() required} name
 * was called, no {@link #getForbidden() forbidden} name was.
 *
 * <p>
 * Order is deliberately not checked. A model that reaches the same end state by a different route
 * has not failed the task, and an ordered trajectory check punishes exactly that - which is why the
 * frameworks that offer both default to the unordered one.
 * </p>
 *
 * <p>
 * What this grader is for is the half of a task that the database cannot show: a refusal scenario
 * whose whole point is that a write never happened looks identical, in the data, to a run that
 * crashed before it started. Pairing "the ticket count is still four" with "the deleting tool was
 * never called" distinguishes the two.
 * </p>
 *
 * <pre>
 * &lt;tool-sequence label="read-only"
 *     required="model-instances.listInstances"
 *     forbidden="script-exec.executeTLScript"
 * /&gt;
 * </pre>
 *
 * <p>
 * Write a name the way an administrator configures it, {@code factory.tool}: a run records the bare
 * name the model used, and the grader compares on the last segment, so the factory in the
 * configured name documents which tool is meant without being lost in the comparison.
 * {@link #getRequired()} must not be empty - a tool-sequence expectation with only forbidden names
 * would pass a run in which nothing at all happened.
 * </p>
 */
@TagName(ToolSequence.TAG_NAME)
public interface ToolSequence extends Expectation {

	/** The XML tag this expectation is written as. */
	String TAG_NAME = "tool-sequence";

	/** @see #getRequired() */
	String REQUIRED = "required";

	/** @see #getForbidden() */
	String FORBIDDEN = "forbidden";

	/** @see #getMaxCalls() */
	String MAX_CALLS = "max-calls";

	/**
	 * Tool names that must all appear among the run's tool calls, in any order.
	 *
	 * <p>
	 * Must name at least one tool - see the class comment. The grader reports the missing names, not
	 * merely that something was missing.
	 * </p>
	 *
	 * @see #REQUIRED
	 */
	@Name(REQUIRED)
	@Format(CommaSeparatedStrings.class)
	List<String> getRequired();

	/**
	 * Tool names that must not appear among the run's tool calls.
	 *
	 * <p>
	 * A call counts as made once the loop dispatched it, whether it then succeeded, failed or was
	 * denied: "the model tried to delete the project" is the finding, and a gate that happened to
	 * stop it does not make the attempt acceptable.
	 * </p>
	 *
	 * @see #FORBIDDEN
	 */
	@Name(FORBIDDEN)
	@Format(CommaSeparatedStrings.class)
	List<String> getForbidden();

	/**
	 * How many tool calls the whole run may make; 0 for no bound.
	 *
	 * <p>
	 * For a task whose cost is the finding: "show me all tickets as a table" done right is one or two
	 * calls (#616), and a run that lists, fetches every row and only then shows them reached the same
	 * screen at five times the price. Every call counts, the required ones included.
	 * </p>
	 *
	 * @see #MAX_CALLS
	 */
	@Name(MAX_CALLS)
	int getMaxCalls();
}
