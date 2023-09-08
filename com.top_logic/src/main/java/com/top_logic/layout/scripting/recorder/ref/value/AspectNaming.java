/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Base class for {@link ModelNamingScheme}s resolving aspects of a given base model.
 * 
 * @param <B>
 *        The type of the base model, an aspect of is resolved.
 * 
 * @see Name#getModel()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AspectNaming<M, N extends AspectNaming.Name, B> extends GlobalModelNamingScheme<M, N> {

	/**
	 * Base name for {@link AspectNaming} names.
	 */
	@Abstract
	public interface Name extends ModelName {

		/**
		 * The referenced base model for which some aspect is extracted.
		 */
		ModelName getModel();

		/**
		 * @see #getModel()
		 */
		void setModel(ModelName value);

	}

	/**
	 * The base type an aspect of is resolved.
	 */
	protected abstract Class<B> baseType();

	@Override
	public Maybe<N> buildName(M model) {
		return Maybe.none();
	}

	@Override
	public M locateModel(ActionContext context, N name) {
		Object baseModelRef = context.resolve(name.getModel());
		ApplicationAssertions.assertInstanceOf(name, "Referenced model:", baseType(), baseModelRef);
		@SuppressWarnings("unchecked")
		B baseModel = (B) baseModelRef;
		return localteModel(context, name, baseModel);
	}

	/**
	 * Locates the aspect of the given base model.
	 * 
	 * @param context
	 *        See {@link #locateModel(ActionContext, Name)}.
	 * @param name
	 *        See {@link #locateModel(ActionContext, Name)}.
	 * @param baseModel
	 *        The referenced base model.
	 * @return See {@link #locateModel(ActionContext, Name)}.
	 */
	protected abstract M localteModel(ActionContext context, N name, B baseModel);

}
