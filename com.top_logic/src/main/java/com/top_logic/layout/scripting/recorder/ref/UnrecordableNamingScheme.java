/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.AssertionPlugin;

/**
 * A {@link ModelNamingScheme} that cannot be recorded generically.
 * <p>
 * It is possible to record it with a custom {@link AssertionPlugin}, though.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class UnrecordableNamingScheme<M, N extends ModelName> extends AbstractModelNamingScheme<M, N> {

	/**
	 * Creates a {@link UnrecordableNamingScheme}.
	 * 
	 * @param modelClass
	 *        Is not allowed to be null.
	 * @param nameClass
	 *        Is not allowed to be null.
	 */
	public UnrecordableNamingScheme(Class<M> modelClass, Class<N> nameClass) {
		super(modelClass, nameClass);

	}

	@Override
	protected void initName(N name, M model) {
		throw new UnsupportedOperationException("The " + getClass().getSimpleName()
			+ " cannot be recorded generically.");
	}

	@Override
	protected boolean isCompatibleModel(M model) {
		return false;
	}

}
