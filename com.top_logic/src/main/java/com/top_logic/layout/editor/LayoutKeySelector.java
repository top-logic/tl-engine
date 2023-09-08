/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.component.AbstractSelectorComponent;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutUtils;

/**
 * {@link LayoutComponent} displaying a single select field with valid layout keys as options.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutKeySelector extends AbstractSelectorComponent {

	/**
	 * Creates a {@link LayoutKeySelector} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public LayoutKeySelector(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected LabelProvider getOptionLabelProvider() {
		return DefaultLabelProvider.INSTANCE;
	}

	@Override
	protected boolean supportsOption(Object value) {
		return value instanceof String && LayoutUtils.isLayout((String) value);
	}

	@Override
	protected List<?> getOptionList() {
		return Arrays.asList(LayoutConstants.MASTER_FRAME_LAYOUT);
	}

}
