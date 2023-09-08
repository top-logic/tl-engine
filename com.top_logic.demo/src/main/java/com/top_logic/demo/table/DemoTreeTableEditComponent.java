/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.table;

import org.xml.sax.Attributes;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.table.tree.TreeTableComponent;

/**
 * {@link EditComponent}, which displays a tree based table.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public final class DemoTreeTableEditComponent extends TreeTableComponent {

	/**
	 * Legacy constructor for creating an {@link DemoTreeTableEditComponent} via {@link Attributes}.
	 */
	@CalledByReflection
	public DemoTreeTableEditComponent(InstantiationContext context, Config attributes) throws ConfigurationException {
		super(context, attributes);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof StructuredElement;
	}

}
