/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.admin.component;

import java.util.Map;

import com.top_logic.basic.Reloadable;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link CommandHandler} for {@link BasicRuntimeModule} whose implementation is a
 * {@link Reloadable}.
 * 
 * <p>
 * The handler restarts resp. starts the module and reloads the newly created implementation.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RestartAndReload extends AbstractCommandHandler {

	/**
	 * Configuration of an {@link RestartAndReload}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a new {@link RestartAndReload}.
	 */
	public RestartAndReload(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		if (model instanceof BasicRuntimeModule<?> module) {
			if (module.isActive()) {
				TLServiceUtils.restartService(module);
				if (!module.isActive()) {
					return HandlerResult.error(TLServiceUtils.errorRestartMessage(module));
				}
			} else {
				TLServiceUtils.startService(module);
				if (!module.isActive()) {
					return HandlerResult.error(TLServiceUtils.errorStartMessage(module));
				}
			}
			ManagedClass mc = module.getImplementationInstance();
			if (mc instanceof Reloadable reloadable) {
				boolean success = reloadable.reload();
				if (!success) {
					return HandlerResult.error(I18NConstants.ERROR_RELOADING_SERVICE__NAME.fill(reloadable.getName()));
				}
				InfoService.showInfo(
					I18NConstants.SERVICE_RELOAD_MESSAGE__SERVICE.fill(MetaLabelProvider.INSTANCE.getLabel(module)));
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
