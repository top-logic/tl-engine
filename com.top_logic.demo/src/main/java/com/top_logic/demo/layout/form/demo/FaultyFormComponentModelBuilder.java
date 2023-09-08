/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder}, that throws an {@link UnsupportedOperationException}, when it will used for
 * model creation.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FaultyFormComponentModelBuilder implements ModelBuilder {

	/**
	 * Singleton {@link FaultyFormComponentModelBuilder} instance.
	 */
	public static final FaultyFormComponentModelBuilder INSTANCE = new FaultyFormComponentModelBuilder();

	private FaultyFormComponentModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		throw new UnsupportedOperationException("Faulty model builder");
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}
