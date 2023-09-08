/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.action.SleepAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ApplicationActionOp} {@link Thread#sleep(long) sleeping} for a given number of
 * milliseconds.
 * 
 * @see SleepAction
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SleepActionOp<S extends SleepAction> extends AbstractApplicationActionOp<S> {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link SleepActionOp}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public SleepActionOp(InstantiationContext context, S config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		Thread.sleep(getConfig().getSleepTimeMillis());
		return argument;
	}

}
