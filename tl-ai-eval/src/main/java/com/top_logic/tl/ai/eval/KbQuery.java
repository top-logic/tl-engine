/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * An {@link Expectation} on the <b>final state of the knowledge base</b>: a TL-Script expression is
 * evaluated after the run and its result must equal a written value.
 *
 * <p>
 * This is the eval's primary grader, and it grades the database rather than the transcript on
 * purpose. A transcript says what the model claimed to do; only the data says what happened. It is
 * also the only grader that survives a model solving the task a way nobody anticipated.
 * </p>
 *
 * <pre>
 * &lt;kb-query label="alpha-has-four-tickets"
 *     expression="count(all(`pm:Ticket`).filter(t -&gt; $t.get(`pm:Ticket#title`) == 'Login fails'))"
 *     expected="1"
 * /&gt;
 * </pre>
 *
 * <p>
 * The comparison is on the string rendering of the result, because a TL-Script expression yields
 * {@code Double}, {@code Integer}, {@code String} or a wrapper depending on how it is written, and
 * a configuration file can only carry text. Write the expression so that it produces a scalar -
 * {@code count(...)}, {@code isEmpty(...)}, a single attribute value - and not a collection whose
 * rendering nobody can predict.
 * </p>
 */
@TagName(KbQuery.TAG_NAME)
public interface KbQuery extends Expectation {

	/** The XML tag this expectation is written as. */
	String TAG_NAME = "kb-query";

	/** @see #getExpression() */
	String EXPRESSION = "expression";

	/** @see #getExpected() */
	String EXPECTED = "expected";

	/**
	 * The TL-Script expression evaluated after the run, as the account the run acted as.
	 *
	 * @see #EXPRESSION
	 */
	@Name(EXPRESSION)
	@Mandatory
	String getExpression();

	/**
	 * The value {@link #getExpression()} must produce, compared as text.
	 *
	 * <p>
	 * Mandatory, and mandatory on purpose: an expectation with no expected value could only ever
	 * assert "the expression ran", which is not a claim about the task.
	 * </p>
	 *
	 * @see #EXPECTED
	 */
	@Name(EXPECTED)
	@Mandatory
	String getExpected();
}
