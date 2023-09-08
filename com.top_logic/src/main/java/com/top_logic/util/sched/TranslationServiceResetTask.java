/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.util.sched.task.impl.StateHandlingTask;

/**
 * A {@link StateHandlingTask} that regularly resets the character counter of the currently active
 * {@link TranslationService}.
 *
 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
 */
@InApp
public final class TranslationServiceResetTask extends StateHandlingTask<StateHandlingTask.Config<?>> {

	/**
	 * Creates a new {@link TranslationServiceResetTask}.
	 */
	public TranslationServiceResetTask(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void runHook() {
		if (TranslationService.isActive()) {
			((TranslationService<?>) TranslationService.getInstance()).resetSummarySize();
		}
	}
}
