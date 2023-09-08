/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.button;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.AssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.SingleValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Superclass for {@link AssertionPlugin} that bases on {@link CommandModel}s identified by a given
 * {@link ModelName}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class LabeledButtonAssertionPlugin<S extends FormField>
		extends SingleValueAssertionPlugin<CommandModel, S> {

	private final ModelName _modelName;

	/**
	 * Creates a new {@link LabeledButtonAssertionPlugin}.
	 * 
	 * @param model
	 *        The {@link CommandModel} to create assertion for.
	 * @param modelName
	 *        {@link ModelName} that identifies the given model.
	 */
	public LabeledButtonAssertionPlugin(CommandModel model, ModelName modelName, boolean assertByDefault,
			String internalName) {
		super(model, assertByDefault, internalName);
		_modelName = modelName;
	}

	/**
	 * Returns the {@link ModelName} for the {@link #getModel() command model}.
	 */
	protected ModelName buttonName() {
		return _modelName;
	}

}
