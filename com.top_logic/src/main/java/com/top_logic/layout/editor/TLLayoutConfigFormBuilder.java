/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.FixedLayout;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * Builds the {@link FormContext} for a layout component configuration.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLLayoutConfigFormBuilder extends TLLayoutFormBuilder {

	/**
	 * Creates a {@link TLLayoutConfigFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLLayoutConfigFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Class<? extends TLLayout> getModelType() {
		return FixedLayout.class;
	}

	@Override
	ConfigurationItem getConfiguration(TLLayout layout) throws ConfigurationException {
		return layout.get();
	}

}
