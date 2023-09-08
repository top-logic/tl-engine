/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;

/**
 * {@link ApplicationAction} restarting an {@link BasicRuntimeModule service}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ServiceRestartAction extends ApplicationAction {

	/**
	 * The service type to restart.
	 */
	Class<? extends BasicRuntimeModule<?>> getService();

	/**
	 * Implementation of {@link ServiceRestartAction}.
	 */
	class Op extends AbstractApplicationActionOp<ServiceRestartAction> {

		public Op(InstantiationContext context, ServiceRestartAction config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {

			BasicRuntimeModule<?> module = ModuleUtil.getModule(getConfig().getService());
			ModuleUtil.INSTANCE.restart(module, null);

			return argument;
		}

	}

}
