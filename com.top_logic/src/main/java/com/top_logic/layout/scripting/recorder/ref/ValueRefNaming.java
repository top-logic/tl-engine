/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} to convert {@link ValueRef} to {@link ModelName}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValueRefNaming extends ModelNamingScheme<Object, Object, ValueRefNaming.ValueRefName> {

	/**
	 * {@link ModelName} holding a {@link ValueRef}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ValueRefName extends ModelName {

		/**
		 * The {@link ValueRef}, this {@link ModelName} is a name for.
		 */
		ModelName getRef();

		/**
		 * Setter for
		 */
		void setRef(ModelName ref);
	}

	@Override
	public Class<Object> getContextClass() {
		return Object.class;
	}

	@Override
	public Class<ValueRefName> getNameClass() {
		return ValueRefName.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Object locateModel(ActionContext context, Object valueContext, ValueRefName name) {
		return context.resolve(name.getRef(), valueContext);
	}

	/**
	 * Never used for recording, replaced by {@link ValueRefNamingDirect}.
	 * 
	 * @see ValueRefNamingDirect#buildName(Object, Object)
	 */
	@Override
	public Maybe<ValueRefName> buildName(Object valueContext, Object model) {
		return Maybe.none();
	}

}
