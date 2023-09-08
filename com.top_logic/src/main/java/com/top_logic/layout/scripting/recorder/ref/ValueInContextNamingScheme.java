/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * A {@link ModelNamingScheme} for referencing a context-local value together with its context.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class ValueInContextNamingScheme extends
		AbstractModelNamingScheme<Object, ValueInContextNamingScheme.ValueInContextName> {

	/**
	 * {@link ModelName} for {@link ValueInContextNamingScheme}.
	 */
	public interface ValueInContextName extends ModelName {

		/**
		 * A context local value.
		 */
		ModelName getLocalName();

		/**
		 * @see #getLocalName()
		 */
		void setLocalName(ModelName localName);

		/**
		 * The context in which {@link #getLocalName()} has to be resolved.
		 */
		ModelName getContextName();

		/**
		 * @see #getContextName()
		 */
		void setContextName(ModelName contextName);

	}

	@Override
	public Class<ValueInContextName> getNameClass() {
		return ValueInContextName.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Object locateModel(ActionContext actionContext, ValueInContextName name) {
		Object context = actionContext.resolve(name.getContextName());
		return actionContext.resolve(name.getLocalName(), context);
	}

	@Override
	protected void initName(ValueInContextName name, Object model) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean isCompatibleModel(Object model) {
		return false;
	}

}
