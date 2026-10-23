/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import org.junit.Assume;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;

/**
 * The switch that turns the real research questions on, and the assumption that makes a build without
 * it report as <b>skipped</b>.
 *
 * <p>
 * One research question is a lead and several workers, each of them many model calls and web
 * requests: a single question costs more than the whole {@code pm} matrix, and the question set takes
 * an hour. So it has a switch of its own, separate from {@link EvalE2e}. The graders it needs run in
 * every build against a scripted model ({@code TestResearchPlumbing}).
 * </p>
 *
 * <p>
 * {@link #assumeEnabled()} throws a violated assumption rather than returning early, and
 * {@link AssumptionAwareRunner} reports that as a skip - a test that returned early would count as
 * passed.
 * </p>
 *
 * <h2>Running it</h2>
 *
 * <pre>
 * export tl_openai_api_key="..."               # the arm
 * export tl_anthropic_api_key="..."            # the pinned judge
 * export tl_brave_api_key="..."                # the workers' web search
 * mvn -pl tl-ai-eval test -DskipTests=false -Dtl.ai.research.e2e=true -Dtest=TestResearchEval
 * </pre>
 *
 * <p>
 * {@code -Dtl.ai.research.arms=name,name} runs some of the arms, {@code -Dtl.ai.research.scenarios=id,id} a sample, {@code -Dtl.ai.research.repeats=1} one run
 * per question, {@code -Dtl.ai.research.judge=model} another judge than the pinned one - whose rows
 * then compare with nothing graded by the pinned one.
 * </p>
 */
public class ResearchE2e {

	/** System property that enables the real research questions. */
	public static final String ENABLED_PROPERTY = "tl.ai.research.e2e";

	/** System property naming the arms to run, comma-separated; all when unset. */
	public static final String ARMS_PROPERTY = "tl.ai.research.arms";

	/** System property naming the scenarios to run, comma-separated; all when unset. */
	public static final String SCENARIOS_PROPERTY = "tl.ai.research.scenarios";

	/** System property overriding the repeats of the file. */
	public static final String REPEATS_PROPERTY = "tl.ai.research.repeats";

	/** System property naming another judge model than the pinned one. */
	public static final String JUDGE_PROPERTY = "tl.ai.research.judge";

	/** System property naming the provider of {@link #JUDGE_PROPERTY}'s model. */
	public static final String JUDGE_PROVIDER_PROPERTY = "tl.ai.research.judge-provider";

	private ResearchE2e() {
		// Static utility.
	}

	/** Skips the calling test unless {@link #ENABLED_PROPERTY} is set. */
	public static void assumeEnabled() {
		Assume.assumeTrue("the research questions are off, enable with -D" + ENABLED_PROPERTY + "=true",
			enabled());
	}

	/** Whether {@link #ENABLED_PROPERTY} is set; for the setup around the methods. */
	public static boolean enabled() {
		return Boolean.getBoolean(ENABLED_PROPERTY);
	}
}
