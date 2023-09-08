/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.editor.TLLayoutFormBuilder.EditModel;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * Command to store the {@link TLLayout} template arguments into the database.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class StoreTemplateArgumentIntoDatabase extends TLLayoutApplyHandler {

	/**
	 * Creates a {@link StoreTemplateArgumentIntoDatabase} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public StoreTemplateArgumentIntoDatabase(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void storeLayout(FormComponent component, EditModel editModel, TLLayout layout) {
		String layoutKey = getLayoutKey(component);

		updateLayoutStorage(layoutKey, getTemplateName(layout), editModel.getConfiguration());

		LayoutTemplateUtils.replaceComponent(layoutKey, component);
	}

	private void updateLayoutStorage(String layoutKey, String templateName, ConfigurationItem arguments) {
		KBUtils.inTransaction(() -> {
			LayoutTemplateUtils.replaceInnerTemplates(arguments);
			TypedConfiguration.minimize(arguments);
			LayoutTemplateUtils.storeLayout(layoutKey, templateName, arguments);
		});
	}


	private String getTemplateName(Object layout) {
		return ((TLLayout) layout).getTemplateName();
	}

	private String getLayoutKey(LayoutComponent component) {
		return (String) component.getSelectableMaster().getSelected();
	}

}
