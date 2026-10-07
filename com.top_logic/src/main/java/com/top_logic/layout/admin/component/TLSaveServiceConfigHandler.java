/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.File;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.autoconf.InAppServiceConfigStore;
import com.top_logic.util.error.TopLogicException;

/**
 * Stores custom service configurations in a separate configuration file in the autoconf folder of
 * the toplevel module.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLSaveServiceConfigHandler extends AbstractApplyCommandHandler {

	/**
	 * The stored configuration replaces the service configuration of the underlying configuration
	 * layers, so that a value removed in the editor does not reappear from the base configuration.
	 */
	private static final boolean OVERRIDE_BASE_CONFIG = true;

	/**
	 * Creates a {@link TLSaveServiceConfigHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLSaveServiceConfigHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean storeChanges(LayoutComponent component, FormContext context, Object model) {
		BasicRuntimeModule<?> module = (BasicRuntimeModule<?>) model;

		if (model instanceof BasicRuntimeModule) {
			ServiceConfiguration<?> serviceConfiguration = getServiceConfiguration(component);
			checkConstraints(serviceConfiguration);
			boolean serviceConfig = storeNewServiceConfiguration(serviceConfiguration, module);

			TLServiceUtils.reloadConfigurations();
			repaintEditor((EditComponent) component);

			InfoService.showInfo(I18NConstants.CONFIGURATION_SAVED_INFO_MESSAGE);

			return serviceConfig;
		}

		return false;
	}

	private void checkConstraints(ServiceConfiguration<?> serviceConfiguration) {
		BufferingI18NLog checkingProtocol = new BufferingI18NLog();
		new ConstraintChecker().check(checkingProtocol, serviceConfiguration);
		ResKey[] errorKeys =
			checkingProtocol.getEntries()
				.stream()
				.filter(event -> event.getLevel() == Level.ERROR)
				.map(BufferingI18NLog.Entry::getMessage)
				.toArray(ResKey[]::new);
		if (errorKeys.length > 0) {
			throw new TopLogicException(I18NConstants.SERVICE_CONFIGURATION_INVALID__ERRORS.fill(errorKeys));
		}
	}

	private void repaintEditor(EditComponent editor) {
		editor.removeFormContext();
		editor.invalidate();
	}

	private ServiceConfiguration<?> getServiceConfiguration(LayoutComponent component) {
		TLServiceConfigEditorFormBuilder.EditModel editModel = getEditModel(component);

		return editModel.getServiceConfiguration();
	}

	private TLServiceConfigEditorFormBuilder.EditModel getEditModel(LayoutComponent component) {
		FormContext formContext = getFormContext(component);

		return (TLServiceConfigEditorFormBuilder.EditModel) EditorFactory.getModel(formContext);
	}

	private FormContext getFormContext(LayoutComponent component) {
		EditComponent editor = (EditComponent) component;

		return editor.getFormContext();
	}

	private boolean storeNewServiceConfiguration(ServiceConfiguration<?> instance,
			BasicRuntimeModule<?> module) {

		try {
			File inAppServiceConfigFile = InAppServiceConfigStore.store(module, instance, OVERRIDE_BASE_CONFIG);
			Logger.info(inAppServiceConfigFile.getAbsolutePath() + " successfully written.",
				TLSaveServiceConfigHandler.class);
		} catch (Exception exception) {
			throw new TopLogicException(I18NConstants.SERVICE_CONFIG_SAVE_ERROR, exception);
		}

		return true;
	}

	static File inAppServiceConfiguration(BasicRuntimeModule<?> model) {
		return InAppServiceConfigStore.fileFor(model);
	}

}
