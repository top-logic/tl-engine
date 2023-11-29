/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.implementation.FormEditorContext;

/**
 * {@link VariableDefinition} for a static internationalized text within a HTML template.
 */
@Label("Internationalized text")
public class I18NTextVariable extends AbstractConfiguredInstance<I18NTextVariable.Config>
		implements VariableDefinition<I18NTextVariable.Config> {

	/**
	 * Configuration options for {@link I18NTextVariable}.
	 */
	@TagName("i18n")
	public interface Config extends VariableDefinition.Config<I18NTextVariable> {

		/**
		 * The internationalized text to render.
		 */
		ResKey getText();

	}

	/**
	 * Creates a {@link I18NTextVariable}.
	 */
	public I18NTextVariable(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object eval(LayoutComponent component, FormEditorContext editorContext, Object model) {
		return getConfig().getText();
	}

}
