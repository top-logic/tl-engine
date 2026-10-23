/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Runs every scenario on every arm, as often as the configuration says, and collects the rows.
 *
 * <p>
 * Sequentially, and deliberately so. Write scenarios serialize on the knowledge base anyway, the
 * {@link EvalFixture} between two runs cannot be shared, and a parallel matrix would make wall-clock
 * time - one of the numbers being measured - depend on how many arms happened to be in flight.
 * </p>
 */
public class EvalMatrix {

	private EvalMatrix() {
		// Static utility.
	}

	/**
	 * Runs the whole matrix.
	 *
	 * <p>
	 * The iteration order is scenario, then arm, then repeat, and the report is written in the same
	 * order. It matters for a long matrix: the first arm's results for a scenario arrive beside the
	 * second's, so a run that has to be stopped early still compares something.
	 * </p>
	 *
	 * @param actor
	 *        The account the runs act as; must be the one bound to the calling thread.
	 * @param fixture
	 *        Reseeds the data before every single run.
	 */
	public static List<RunRecord> run(EvalConfig config, Person actor, EvalFixture fixture) {
		return run(config, actor, fixture, Judge.of(config));
	}

	/**
	 * Runs the whole matrix with the given judge in place of the one the file names - for a run that
	 * has no access to the pinned judge, or a plumbing test with a scripted one. Rows graded by
	 * another judge are not comparable with rows graded by the pinned one.
	 *
	 * @see #run(EvalConfig, Person, EvalFixture)
	 */
	public static List<RunRecord> run(EvalConfig config, Person actor, EvalFixture fixture, Judge judge) {
		EvalConfigs.check(config);
		List<RunRecord> rows = new ArrayList<>();
		for (EvalScenario scenario : config.getScenarios()) {
			int repeats = EvalConfigs.repeatsOf(config, scenario);
			for (EvalConfig.Arm arm : EvalConfigs.armsOf(config, scenario)) {
				String unusable = EvalRunner.whyUnusable(arm);
				if (unusable != null) {
					// Once per cell, not once per repeat: repeating a run that cannot happen would
					// put a bigger number in the "could not run" column for no more information.
					Logger.info("Eval arm '" + arm.getName() + "' is skipped: " + unusable,
						EvalMatrix.class);
					rows.add(RunRecord.couldNotRun(scenario.getId(), arm, 1, unusable));
					continue;
				}
				for (int repeat = 1; repeat <= repeats; repeat++) {
					fixture.reset();
					rows.add(EvalRunner.run(scenario, arm, repeat, actor, judge));
				}
			}
		}
		return rows;
	}
}
