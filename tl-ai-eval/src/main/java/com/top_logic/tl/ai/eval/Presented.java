/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.tl.ai.core.tools.present.ChartKind;
import com.top_logic.tl.ai.core.tools.present.PresentAs;

/**
 * An {@link Expectation} on what the run <b>showed the user</b>: its last {@code present} call
 * showed the right objects, the right way.
 *
 * <p>
 * The counterpart of {@link KbQuery} for a task that changes no data. "Show me all tickets as a
 * table" leaves the database as it was, so a grader of the data passes a run that did nothing; what
 * the task produced is the presentation, and that is graded here. The grader does not trust the
 * model's description of it: it evaluates the query the model passed - or resolves the ids - itself,
 * through the same executor the tool uses, and checks the objects that come out.
 * </p>
 *
 * <pre>
 * &lt;presented label="all-tickets-as-a-table" as="TABLE"&gt;
 *     &lt;expression&gt;rows -&gt; values -&gt; $rows.size() == all(`pm:Ticket`).size() &amp;&amp; $rows.containsAll(all(`pm:Ticket`))&lt;/expression&gt;
 *     &lt;expected&gt;true&lt;/expected&gt;
 * &lt;/presented&gt;
 * </pre>
 *
 * <p>
 * The expression is a function of two lists: the presented objects, and - for a chart - the values
 * the model's value function computes for them, in the same order (empty otherwise), so that a chart
 * of the right objects drawing the wrong number fails. Its result is compared as text, the way
 * {@link KbQuery}'s is.
 * </p>
 */
@TagName(Presented.TAG_NAME)
public interface Presented extends Expectation {

	/** The XML tag this expectation is written as. */
	String TAG_NAME = "presented";

	/** @see #getAs() */
	String AS = "as";

	/** @see #getChartKind() */
	String CHART_KIND = "chart-kind";

	/** @see #getExpression() */
	String EXPRESSION = "expression";

	/** @see #getExpected() */
	String EXPECTED = "expected";

	/**
	 * How the objects must have been shown; empty for any way.
	 *
	 * <p>
	 * Compared with what the tool made of the call, so a call that named no {@code as} and showed
	 * several objects counts as a {@link PresentAs#TABLE table}.
	 * </p>
	 */
	@Name(AS)
	@Nullable
	@NullDefault
	PresentAs getAs();

	/**
	 * The kind of chart it must have been; empty for any. Only meaningful with {@code as="CHART"}.
	 */
	@Name(CHART_KIND)
	@Nullable
	@NullDefault
	ChartKind getChartKind();

	/**
	 * TL-Script function of the presented objects and the chart's values,
	 * {@code rows -> values -> ...}, evaluated after the run.
	 *
	 * @see #EXPRESSION
	 */
	@Name(EXPRESSION)
	@Mandatory
	String getExpression();

	/**
	 * The value {@link #getExpression()} must produce, compared as text.
	 *
	 * @see #EXPECTED
	 */
	@Name(EXPECTED)
	@Mandatory
	String getExpected();
}
