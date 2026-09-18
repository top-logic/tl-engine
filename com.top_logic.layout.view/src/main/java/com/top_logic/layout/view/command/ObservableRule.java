/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

/**
 * Optional mix-in for a {@link ViewExecutabilityRule} that can say when its answer may have changed.
 *
 * <p>
 * A command follows its own input and the object that input holds, which covers every rule deciding
 * by those. A rule deciding by something else - the validation state of the form it sits in, say -
 * announces that state changing here, and the command's {@link ViewCommandModel} re-evaluates its
 * rules for as long as it is attached.
 * </p>
 *
 * <p>
 * A rule that resolves what it watches from the context of the command combines this with
 * {@link ContextDependentRule}: the binding happens first, so what it captures is what is watched.
 * </p>
 */
public interface ObservableRule {

	/**
	 * Begins reporting changes of this rule's answer.
	 *
	 * @param revalidate
	 *        Run whenever the rule may answer differently than before.
	 * @return Stops the reporting again; never {@code null}.
	 */
	Runnable observe(Runnable revalidate);
}
