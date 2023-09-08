/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Base class for {@link ModelNamingScheme}s not requiring any special context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GlobalModelNamingScheme<M, N extends ModelName> extends ModelNamingScheme<Object, M, N> {

	/**
	 * Creates a {@link GlobalModelNamingScheme}.
	 * 
	 * See {@link ModelNamingScheme#ModelNamingScheme(Class, Class, Class)} for details.
	 */
	protected GlobalModelNamingScheme(Class<M> modelClass, Class<? extends N> nameClass) {
		super(modelClass, nameClass, Object.class);
	}

	/**
	 * Creates a {@link GlobalModelNamingScheme}.
	 * 
	 * See {@link ModelNamingScheme#ModelNamingScheme()} for details.
	 */
	protected GlobalModelNamingScheme() {
		this(null, null);
	}

	@Override
	public Maybe<N> buildName(Object valueContext, M model) {
		return buildName(model);
	}

	@Override
	public final M locateModel(ActionContext context, Object valueContext, N name) {
		return locateModel(context, name);
	}

	/**
	 * Creates a globally resolvable {@link ModelName} for the given model.
	 * 
	 * <p>
	 * This method can serve as implementation of {@link NamedModel#getModelName()} in
	 * {@link NamedModel} instances.
	 * </p>
	 * 
	 * @param model
	 *        The model to be named.
	 * @return The {@link ModelName} of the given model.
	 */
	public abstract Maybe<N> buildName(M model);

	/**
	 * Globally locates the model that is identified by the given {@link ModelName}.
	 * 
	 * @param context
	 *        The context in which the given name should be resolved.
	 * @param name
	 *        The name of the model.
	 * 
	 * @return the located named model.
	 */
	public abstract M locateModel(ActionContext context, N name);

}
