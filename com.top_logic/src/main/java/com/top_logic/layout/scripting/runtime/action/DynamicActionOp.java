/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.DynamicAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Composite {@link ApplicationActionOp} that dynamically {@link #createActions(ActionContext)
 * creates} the concrete actions to execute.
 * 
 * @see #createActions(ActionContext)
 * @see DynamicAction
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class DynamicActionOp<T extends DynamicAction> extends AbstractApplicationActionOp<T> {

	/**
	 * Creates a {@link DynamicActionOp} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DynamicActionOp(InstantiationContext context, T config) {
		super(context, config);
	}

	@Override
	protected final Object processInternal(ActionContext context, Object argument) throws Throwable {
		// Must not be called, because the testing framework should explicitly process the
		// replacement actions created in createActions().
		throw new UnsupportedOperationException();
	}

	/**
	 * Dynamically create actions.
	 * 
	 * @param context
	 *        The {@link ActionContext}.
	 * @return The dynamically created actions.
	 */
	public abstract List<ApplicationAction> createActions(ActionContext context);

}
