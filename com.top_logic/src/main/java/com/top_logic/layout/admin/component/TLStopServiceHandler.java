/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * Stops the given and all dependent TL services.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLStopServiceHandler extends AbstractStopServiceHandler {

	/**
	 * Creates a {@link TLStopServiceHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLStopServiceHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult handleCommandInternal(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		stopService((BasicRuntimeModule<?>) model);

		component.invalidate();

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected HTMLFragment getTitle(Object model) {
		return Fragments.message(I18NConstants.SERVICE_STOP_COMMAND_LABEL);
	}

	private void stopService(BasicRuntimeModule<?> module) {
		try {
			ModuleUtil.INSTANCE.shutDown(module);
		} catch (IllegalArgumentException exception) {
			throw new TopLogicException(I18NConstants.SERVICE_STOP_ERROR.fill(getServiceName(module)), exception);
		}
	}

	private String getServiceName(BasicRuntimeModule<?> module) {
		return ModuleUtil.INSTANCE.getModuleName(module);
	}

}
