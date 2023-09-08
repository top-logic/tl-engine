/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.util.Map;

import com.top_logic.element.layout.formeditor.builder.ConfiguredDynamicFormBuilder;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.ExecutableState.CommandVisibility;

/**
 * {@link ExecutabilityRule} for {@link CommandHandler} that must only be executable when there is a
 * form to reset.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class HasCustomizedForm implements ExecutabilityRule {
	/**
	 * Singleton {@link HasCustomizedForm} instance.
	 */
	public static final HasCustomizedForm INSTANCE = new HasCustomizedForm();

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (!(aComponent instanceof FormComponent)) {
			return new ExecutableState(CommandVisibility.DISABLED, I18NConstants.NO_FORM_COMPONENT_ERROR);
		}

		FormComponent form = (FormComponent) aComponent;
		ModelBuilder builder = form.getBuilder();
		if (!builder.supportsModel(model, aComponent)) {
			return ExecutableState.NO_EXEC_NOT_SUPPORTED;
		}

		if (!(builder instanceof ConfiguredDynamicFormBuilder)) {
			return new ExecutableState(CommandVisibility.DISABLED, I18NConstants.UNSUITABLE_BUILDER_ERROR);
		}

		if (((ConfiguredDynamicFormBuilder) builder).displaysStandardForm()) {
			return new ExecutableState(CommandVisibility.DISABLED, I18NConstants.NO_FORM_TO_RESET_ERROR);
		}

		return ExecutableState.EXECUTABLE;
	}
}
