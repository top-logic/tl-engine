/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.provider.label.TypeSafeLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;

/**
 * Provides the label of the component given by a {@link LayoutComponent} using the
 * {@link LayoutComponent#getTitleKey()}.
 */
public class ComponentLabelProvider extends TypeSafeLabelProvider<LayoutComponent> {

	/**
	 * Singleton {@link ComponentLabelProvider} instance.
	 */
	public static final ComponentLabelProvider INSTANCE = new ComponentLabelProvider();

	@Override
	protected Class<LayoutComponent> getObjectType() {
		return LayoutComponent.class;
	}

	@Override
	protected String getNonNullLabel(LayoutComponent component) {
		return LayoutUtils.getLabel(component);
	}

}
