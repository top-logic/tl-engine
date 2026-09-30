/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.Location;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.TextInputControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link AbstractStaticInfoPlugin} displaying a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentInfoPlugin extends AbstractStaticInfoPlugin<LayoutComponent> {

	/**
	 * Creates a new {@link ComponentInfoPlugin}.
	 */
	public ComponentInfoPlugin(LayoutComponent model, ResPrefix resPrefix, String internalName) {
		super(model, resPrefix, internalName);
	}

	@Override
	protected FormField createValueField(LayoutComponent model, String fieldName) {
		StringField field = FormFactory.newStringField(fieldName, FormFactory.IMMUTABLE);
		TextInputControlProvider cp = new TextInputControlProvider();
		cp.setMultiLine(true);
		field.setControlProvider(cp);
		field.initializeField(getFieldValue(model));
		return field;
	}

	private String getFieldValue(LayoutComponent model) {
		StringBuilder content = new StringBuilder();
		content.append(model.getName());
		content.append('(');
		content.append(model.getClass().getName());
		content.append(')');
		Location location = model.getConfig().location();
		String resource = location.getResource();
		if (!StringServices.isEmpty(resource)) {
			int pathStart = resource.indexOf('(');
			int pathEnd = resource.lastIndexOf(')');
			if (pathStart > -1 & pathEnd > -1 & pathEnd > pathStart) {
				String[] locations =
					resource.substring(pathStart + 1, pathEnd).split(ConfigurationReader.DEFINITION_FILES_SEPARATOR);
				for (int i = locations.length - 1; i >= 0; i--) {
					content.append("\n * ");
					content.append(locations[i]);
				}
			} else {
				content.append(resource);
			}
		}
		return content.toString();
	}

}

