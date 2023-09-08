/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.col.Maybe;

/**
 * Base class for {@link ModelNamingScheme}s that accept all objects of {@link #getModelClass()}
 * type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractModelNamingScheme<M, N extends ModelName> extends GlobalModelNamingScheme<M, N> {

	/**
	 * Creates a {@link AbstractModelNamingScheme}.
	 * 
	 * See {@link ModelNamingScheme#ModelNamingScheme(Class, Class, Class)} for details.
	 */
	protected AbstractModelNamingScheme(Class<M> modelClass, Class<? extends N> nameClass) {
		super(modelClass, nameClass);
	}

	/**
	 * Creates a {@link AbstractModelNamingScheme}.
	 * 
	 * See {@link ModelNamingScheme#ModelNamingScheme()} for details.
	 * 
	 * @deprecated Use {@link #AbstractModelNamingScheme(Class, Class)}
	 */
	@Deprecated
	protected AbstractModelNamingScheme() {
		this(null, null);
	}

	@Override
	public final Maybe<N> buildName(M model) {
		if (isCompatibleModel(model)) {
			N name = createName();
			initName(name, model);
			return Maybe.some(name);
		} else {
			return Maybe.none();
		}
	}

	/**
	 * Initializes the given {@link ModelName} so that it identifies the given model.
	 * 
	 * @param name
	 *        The {@link ModelName} to be initialized.
	 * @param model
	 *        The model that should be identified by the given {@link ModelName}.
	 */
	protected abstract void initName(N name, M model);

	/**
	 * Checks whether the model has any problems that would prevent the {@link ModelNamingScheme}
	 * from identifying it. Subclasses should override this method if they need their models to
	 * fulfill certain preconditions. If not overridden, returns always true.
	 * 
	 * @param model
	 *        The model to be checked.
	 * @return Is the model compatible?
	 */
	protected boolean isCompatibleModel(M model) {
		return true;
	}

}
