/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import com.top_logic.basic.config.TypedConfiguration;

/**
 * Building an {@link EvalConfig.Arm} in Java, so that a test asserting how an arm is <em>reported</em>
 * does not first have to write a configuration file for one.
 *
 * <p>
 * A test about a real eval uses the shipped scenario file instead - see {@code TestEvalHarness}.
 * This is for the tests of the CSV and the report, which need an arm that never existed: one naming
 * a model nobody configured.
 * </p>
 */
public class Arms {

	private Arms() {
		// Static utility.
	}

	/** An arm of the given name, agent and model. */
	public static EvalConfig.Arm of(String name, String agent, String model) {
		EvalConfig.Arm result = TypedConfiguration.newConfigItem(EvalConfig.Arm.class);
		result.update(result.descriptor().getProperty(EvalConfig.Arm.NAME_ATTRIBUTE), name);
		result.update(result.descriptor().getProperty(EvalConfig.Arm.AGENT), agent);
		result.update(result.descriptor().getProperty(EvalConfig.Arm.MODEL), model);
		return result;
	}
}
