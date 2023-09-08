/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.layout.themeedit.browser.resource.ThemeResource;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} creating a list of all resources in a given
 * {@link ThemeResource} directory.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeResourcesListBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link ThemeResourcesListBuilder} instance.
	 */
	public static final ThemeResourcesListBuilder INSTANCE = new ThemeResourcesListBuilder();

	private ThemeResourcesListBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
			return Collections.emptyList();
		}

		ThemeResource dir = (ThemeResource) businessModel;

		return dir.list();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null || aModel instanceof ThemeResource;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof ThemeResource && (!((ThemeResource) listElement).isDirectory());
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return ((ThemeResource) listElement).getParent();
	}

}
