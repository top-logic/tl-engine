/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.label.I18NClassNameProvider;
import com.top_logic.layout.provider.label.SimpleClassNameProvider;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} provides the label with the javadoc comment of the given service in a
 * tooltip.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLServiceResourceProvider extends AbstractResourceProvider {

	/**
	 * Singleton {@link TLServiceResourceProvider} instance.
	 */
	public static final TLServiceResourceProvider INSTANCE = new TLServiceResourceProvider();

	@Override
	public String getLabel(Object object) {
		Class<?> implementationClass = implementationClass(object);
		if (implementationClass != null) {
			String label = I18NClassNameProvider.INSTANCE.getLabel(implementationClass);

			if (StringServices.isEmpty(label)) {
				return SimpleClassNameProvider.INSTANCE.getLabel(implementationClass);
			}
			return label;
		}
		return super.getLabel(object);

	}

	private Class<?> implementationClass(Object object) {
		if (object instanceof BasicRuntimeModule<?>) {
			return ((BasicRuntimeModule<?>) object).getImplementation();
		}
		if (object instanceof Class<?>) {
			// Here the implementation class is given directly.
			return (Class<?>) object;
		}
		return null;
	}

	@Override
	public String getTooltip(Object object) {
		Class<?> implementationClass = implementationClass(object);
		if (implementationClass != null) {
			ResKey tooltip = ResKey.forClass(implementationClass).tooltipOptional();

			return Resources.getInstance().getString(tooltip);
		}
		return super.getTooltip(object);
	}

}
