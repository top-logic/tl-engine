/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * Configuration of an action with a result that runs follow-up actions over that result.
 *
 * <p>
 * The arguments of an action chain flow unchanged through the chain; the result of an action does
 * not reach the actions following it. To work with the result, the actions are placed into the
 * {@link #getWithResult()} of the action producing it. They run after the action has completed -
 * including a commit the action does itself - with the arguments of the chain, extended by the
 * result under the name {@link #RESULT} or the name given by {@link WithResult#getAs()}.
 * </p>
 *
 * <p>
 * Example: persist a draft, show the created object and close the dialog:
 * </p>
 *
 * <pre>
 * &lt;persist-transient&gt;
 *   &lt;with-result&gt;
 *     &lt;write-channel name="selection"/&gt;
 *     &lt;close-dialog/&gt;
 *   &lt;/with-result&gt;
 * &lt;/persist-transient&gt;
 * </pre>
 *
 * @implNote Offered by the configurations of actions with a useful result. The follow-up actions are
 *           run by the chain step {@link ViewActions#instantiate(com.top_logic.basic.config.InstantiationContext, List, ActionScope)}
 *           wraps the action into, not by the action itself.
 */
public interface WithResultActions extends ConfigPart {

	/** Configuration name for {@link #getWithResult()}. */
	String WITH_RESULT = "with-result";

	/**
	 * Name of the result of an action for its follow-up actions when no {@link WithResult#getAs()}
	 * is given.
	 */
	String RESULT = "result";

	/**
	 * The actions to run over the result of this action once it has completed.
	 */
	@Name(WITH_RESULT)
	WithResult getWithResult();

	/**
	 * The name of the result of the given action for its follow-up actions: the
	 * {@link WithResult#getAs() given name}, or {@value #RESULT} without one.
	 */
	static String resultName(WithResultActions config) {
		WithResult withResult = config.getWithResult();
		return withResult == null ? RESULT : WithResult.resultName(withResult);
	}

}
