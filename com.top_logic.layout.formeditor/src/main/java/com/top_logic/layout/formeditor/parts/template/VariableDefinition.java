/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.formeditor.parts.template.HTMLTemplateFormProvider.TemplateConfig;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.implementation.FormEditorContext;

/**
 * Algorithm defining the value of a template variable.
 */
public interface VariableDefinition<C extends VariableDefinition.Config<?>> extends ConfiguredInstance<C> {

	/**
	 * The configuration of a {@link VariableDefinition}.
	 */
	@Abstract
	interface Config<I extends VariableDefinition<?>> extends PolymorphicConfiguration<I>, ConfigPart {

		/**
		 * @see #getVariableOwner()
		 */
		String VARIABLE_OWNER = "variable-owner";

		/**
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * The template defining this variable.
		 */
		@Container
		@Hidden
		@Name(VARIABLE_OWNER)
		TemplateConfig getVariableOwner();

		/**
		 * The name of the variable to bind.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

	}

	/**
	 * The name of the variable being defined.
	 */
	default String getName() {
		return getConfig().getName();
	}

	/**
	 * Computes the value of the template variable.
	 * 
	 * @param component
	 *        The context component.
	 * @param editorContext
	 *        The currently displayed editor.
	 * @param model
	 *        The currently rendered object.
	 *
	 * @return The value that should be rendered when expanding the template variable.
	 */
	Object eval(LayoutComponent component, FormEditorContext editorContext, Object model);

}
