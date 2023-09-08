/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.layout.component.ComponentModelNamingScheme.ComponentModelName;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Locate the model of a {@link LayoutComponent}.
 * <p>
 * This {@link ModelNamingScheme} is never used to automatically create a name. It is can only be
 * used to resolve manually created names.
 * </p>
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ComponentModelNamingScheme extends AbstractModelNamingScheme<Object, ComponentModelName> {

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Class<ComponentModelName> getNameClass() {
		return ComponentModelName.class;
	}

	@Override
	protected boolean isCompatibleModel(Object model) {
		return false;
	}

	@Override
	protected void initName(ComponentModelName name, Object model) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object locateModel(ActionContext context, ComponentModelName name) {
		LayoutComponent component = (LayoutComponent) context.resolve(name.getComponent());
		return component.getModel();
	}

	/** {@link ModelName} for the {@link ComponentModelNamingScheme}. */
	public static interface ComponentModelName extends ModelName {

		/**
		 * Component to get the model from.
		 */
		ModelName getComponent();

		/** @see #getComponent() */
		void setComponent(ModelName component);
	}

}
