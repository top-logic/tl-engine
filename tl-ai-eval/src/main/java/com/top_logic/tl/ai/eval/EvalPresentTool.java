/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.tools.present.PresentTool;
import com.top_logic.tl.ai.core.tools.present.Presentation;

/**
 * The {@code present} tool of an eval run, which has no page: it accepts what is presented, as a
 * page would.
 *
 * <p>
 * Everything a model can get wrong about a presentation - a query that fails, ids that name
 * nothing, a chart whose values are not numbers - is checked by {@link PresentTool} before it is
 * delivered, so an eval run is answered exactly as a chat run is. Only the page is missing, and a
 * model told "no page is attached" would go on to answer in text: the run would measure that
 * detour instead of the task. What was shown is graded from the call itself ({@link Presented}),
 * so nothing needs to be kept here.
 * </p>
 *
 * <p>
 * Register it as the factory {@code present} in an application's eval configuration - where no
 * {@code tl-chat} is, which registers its own.
 * </p>
 */
public class EvalPresentTool extends PresentTool {

	/**
	 * Creates an {@link EvalPresentTool}.
	 */
	public EvalPresentTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	@Override
	protected String deliver(Presentation presentation, String channel, ToolContext context) {
		return null;
	}
}
