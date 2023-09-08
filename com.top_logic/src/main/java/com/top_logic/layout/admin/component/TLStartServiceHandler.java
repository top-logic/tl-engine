/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.NullModelDisabled;
import com.top_logic.util.error.TopLogicException;

/**
 * Starts the selected TL service.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLStartServiceHandler extends AbstractCommandHandler {

	/**
	 * Configuration of an {@link TLStartServiceHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a {@link TLStartServiceHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLStartServiceHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		BasicRuntimeModule<?> service = (BasicRuntimeModule<?>) model;
		startService(service);
		component.invalidate();

		return HandlerResult.DEFAULT_RESULT;
	}

	private void startService(BasicRuntimeModule<?> module) {
		try {
			ModuleUtil.INSTANCE.startUp(module);
		} catch (IllegalArgumentException | ModuleException exception) {
			throw new TopLogicException(I18NConstants.SERVICE_START_ERROR.fill(getServiceName(module)), exception);
		}
	}

	private String getServiceName(BasicRuntimeModule<?> module) {
		return ModuleUtil.INSTANCE.getModuleName(module);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(),
			NullModelDisabled.INSTANCE,
			notActiveRule());
	}

	private ExecutabilityRule notActiveRule() {
		return new ExecutabilityRule() {

			@Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model,
					Map<String, Object> someValues) {
				BasicRuntimeModule<?> module = (BasicRuntimeModule<?>) model;
				if (module.isActive()) {
					return ExecutableState.createDisabledState(I18NConstants.SERVICE_ALREADY_STARTED__SERVICE
						.fill(AbstractStopServiceHandler.moduleLabel(module)));
				}
				return ExecutableState.EXECUTABLE;
			}
		};
	}

}
