/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.io.File;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.editor.TLLayoutFormBuilder.EditModel;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * Command to write the {@link com.top_logic.mig.html.layout.LayoutComponent.Config} into the
 * filesystem.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class WriteComponentConfigurationIntoFilesystem extends TLLayoutApplyHandler {

	/**
	 * Creates a {@link WriteComponentConfigurationIntoFilesystem} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public WriteComponentConfigurationIntoFilesystem(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void storeLayout(FormComponent component, EditModel editModel, TLLayout layout) {
		File file = new File(LayoutUtils.getCurrentTopLayoutBaseDirectory(), editModel.getLayoutKey());

		try {
			LayoutComponent.Config config = (LayoutComponent.Config) editModel.getConfiguration();
			TypedConfiguration.minimize(config);
			LayoutUtils.writeConfiguration(config, file);
			LayoutTemplateUtils.replaceComponent(editModel.getLayoutKey(), component);
		} catch (Exception exception) {
			Logger.error("Configuration could not be written into " + file.getAbsolutePath(), exception, this);
		}
	}
}
