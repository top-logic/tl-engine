/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.component.Selectable;

/**
 * {@link FormComponent} holding an additional {@link Selectable#getSelected() selection}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelectableFormComponent extends FormComponent implements Selectable {

	/**
	 * Configuration options for {@link SelectableFormComponent}.
	 */
	public interface Config extends FormComponent.Config, Selectable.SelectableConfig {
		// Sum interface.
	}

	/**
	 * Creates a new {@link SelectableFormComponent}.
	 */
	public SelectableFormComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

}

