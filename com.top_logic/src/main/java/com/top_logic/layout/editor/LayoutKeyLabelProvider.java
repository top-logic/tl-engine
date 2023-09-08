/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.provider.label.TypeSafeLabelProvider;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.util.error.TopLogicException;

/**
 * Resolves the {@link TLLayout} for a given layout key and returns its label.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutKeyLabelProvider extends TypeSafeLabelProvider<String> {

	/**
	 * Singleton {@link LayoutKeyLabelProvider} instance.
	 */
	public static final LayoutKeyLabelProvider INSTANCE = new LayoutKeyLabelProvider();

	@Override
	protected Class<String> getObjectType() {
		return String.class;
	}

	@Override
	protected String getNonNullLabel(String layoutKey) {
		TLLayout layout = LayoutStorage.getInstance().getLayout(layoutKey);

		try {
			return LayoutUtils.getLabel(layout.get());
		} catch (ConfigurationException exception) {
			throw new TopLogicException(I18NConstants.REPLACE_COMPONENT_ERROR__LAYOUT_KEY.fill(layoutKey), exception);
		}
	}

}
