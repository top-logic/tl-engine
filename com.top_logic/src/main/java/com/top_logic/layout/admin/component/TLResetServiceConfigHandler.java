/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.File;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.provider.label.I18NClassNameProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.ConfirmCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Removes the in app custom service configuration from the autoconf directory.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLResetServiceConfigHandler extends ConfirmCommandHandler {

	/**
	 * Configuration of an {@link TLResetServiceConfigHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfirmCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a {@link TLResetServiceConfigHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLResetServiceConfigHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	private void repaintEditor(EditComponent editor) {
		editor.removeFormContext();
		editor.invalidate();
	}

	private Class<? extends ManagedClass> getModuleImplementation(BasicRuntimeModule<?> runtimeModule) {
		return runtimeModule.getImplementation();
	}

	@Override
	protected HandlerResult internalHandleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> args) {
		File inappServiceConfig = TLSaveServiceConfigHandler.inAppServiceConfiguration((BasicRuntimeModule<?>) model);

		if (inappServiceConfig.exists()) {
			if (!inappServiceConfig.delete()) {
				Logger.error("In-App service configuration file " + inappServiceConfig.getAbsolutePath()
					+ " could not be deleted.", TLResetServiceConfigHandler.class);
			}

			TLServiceUtils.reloadConfigurations();
			repaintEditor((EditComponent) component);

			InfoService.showInfo(I18NConstants.CONFIGURATION_RESET_INFO_MESSAGE);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ResKey getConfirmMessage(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> args) {
		ResKey2 confirmMessageKey = com.top_logic.tool.boundsec.I18NConstants.DEFAULT_CONFIRM_MESSAGE__COMMAND_MODEL;
		String modelLabel = I18NClassNameProvider.INSTANCE.getLabel(getModuleImplementation((BasicRuntimeModule<?>) model));

		return confirmMessageKey.fill(getResourceKey(component), modelLabel);
	}

}
