/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.label.TypeSafeLabelProvider;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Provides the label of the component given by a {@link ComponentName} using the
 * {@link LayoutComponent#getTitleKey()}.
 * 
 * @see ComponentName
 * @see LabelProvider
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ComponentNameLabelProvider extends TypeSafeLabelProvider<ComponentName> {

	/**
	 * Singleton {@link ComponentNameLabelProvider} instance.
	 */
	public static final ComponentNameLabelProvider INSTANCE = new ComponentNameLabelProvider();

	@Override
	protected Class<ComponentName> getObjectType() {
		return ComponentName.class;
	}

	@Override
	protected String getNonNullLabel(ComponentName name) {
		String result = getLabel(name);

		if (StringServices.isEmpty(result)) {
			return MetaResourceProvider.INSTANCE.getLabel(name);
		}

		return result;
	}

	private String getLabel(ComponentName name) {
		LayoutComponent component = getComponent(name);

		if (component == null) {
			return "UNRESOLVED: " + name;
		}

		return LayoutUtils.getLabel(component);
	}

	private LayoutComponent getComponent(ComponentName name) {
		return MainLayout.getDefaultMainLayout().getComponentByName(name);
	}

}
