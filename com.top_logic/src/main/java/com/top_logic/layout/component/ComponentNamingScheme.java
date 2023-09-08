/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelNamingScheme} that resolves {@link LayoutComponent}s by name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ComponentNamingScheme extends
		ComponentBasedNamingScheme<LayoutComponent, ComponentBasedNamingScheme.ComponentName> {

	/**
	 * Singleton {@link ComponentNamingScheme} instance.
	 */
	public static final ComponentNamingScheme INSTANCE = new ComponentNamingScheme();

	private ComponentNamingScheme() {
		super(LayoutComponent.class, ComponentName.class);
	}

	@Override
	protected LayoutComponent getContextComponent(LayoutComponent model) {
		return model;
	}

	@Override
	public LayoutComponent locateModel(ActionContext context, ComponentName name) {
		return locateComponent(context, name);
	}

}