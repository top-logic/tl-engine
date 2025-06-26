/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.DefaultSelectionModelFactory;
import com.top_logic.mig.html.TreeSelectionModelFactory;

/**
 * {@link DefaultSelectionModelFactory} that unwraps grid rows before passing the nodes to the
 * configured filter.
 */
public class GridSelectionModelFactory extends TreeSelectionModelFactory {

	/**
	 * Creates a {@link GridSelectionModelFactory}.
	 */
	public GridSelectionModelFactory(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected Object unwrap(Object value) {
		Object nodeValue = super.unwrap(value);

		return nodeValue instanceof FormGroup group ? GridComponent.getRowObject(group) : nodeValue;
	}

}
