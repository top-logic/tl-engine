/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Stores custom service configurations in a separate configuration file in the autoconf folder of
 * the toplevel module.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLSaveServiceConfigHandler extends AbstractApplyCommandHandler {

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
		if (model instanceof BasicRuntimeModule<?> module) {
			ServiceConfiguration<?> serviceConfiguration = getServiceConfiguration(component);
			TLServiceUtils.storeServiceConfiguration(module, serviceConfiguration);
			repaintEditor((EditComponent) component);

			InfoService.showInfo(I18NConstants.CONFIGURATION_SAVED_INFO_MESSAGE);

			return true;
		}

		return false;
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

}
