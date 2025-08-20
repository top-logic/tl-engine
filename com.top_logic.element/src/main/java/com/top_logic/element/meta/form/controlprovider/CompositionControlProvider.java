/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.controlprovider;

import com.top_logic.element.meta.form.fieldprovider.CompositionFieldProvider.Composite;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} creating a {@link Control} to display a {@link Composite} within a
 * dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompositionControlProvider implements ControlProvider {

	/** Singleton {@link CompositionControlProvider} instance. */
	public static final CompositionControlProvider INSTANCE = new CompositionControlProvider();

	private CompositionControlProvider() {
		// singleton instance
	}

	@Override
	public Control createControl(Object model, String style) {
		if (model instanceof Composite composite) {
			return new CompositionDisplayControl(composite);
		} else {
			return new SimpleConstantControl<>(model, ResourceRenderer.INSTANCE);
		}
	}

}

