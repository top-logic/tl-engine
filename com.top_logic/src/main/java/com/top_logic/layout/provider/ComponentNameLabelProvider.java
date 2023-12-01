/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;
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
public class ComponentNameLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link ComponentNameLabelProvider} instance.
	 */
	public static final ComponentNameLabelProvider INSTANCE = new ComponentNameLabelProvider();

	@Override
	public String getLabel(Object object) {
		if (object instanceof ComponentName) {
			return labelNonEmpty((ComponentName) object);
		}
		if (object instanceof LayoutComponent) {
			return labelNonEmpty((LayoutComponent) object);
		}
		return null;
	}

	private String labelNonEmpty(ComponentName name) {
		return nonEmpty(name, label(name));
	}

	private String labelNonEmpty(LayoutComponent component) {
		return nonEmpty(component.getName(), label(component));
	}

	private String nonEmpty(ComponentName name, String result) {
		if (StringServices.isEmpty(result)) {
			return MetaResourceProvider.INSTANCE.getLabel(name);
		}

		return result;
	}

	private String label(ComponentName name) {
		LayoutComponent component = lookupComponent(name);

		if (component == null) {
			return "UNRESOLVED: " + name;
		}

		return label(component);
	}

	private String label(LayoutComponent component) {
		return LayoutUtils.getLabel(component);
	}

	private LayoutComponent lookupComponent(ComponentName name) {
		return MainLayout.getDefaultMainLayout().getComponentByName(name);
	}

}
