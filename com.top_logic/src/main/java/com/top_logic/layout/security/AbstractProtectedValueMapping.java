/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import com.top_logic.basic.col.Mapping;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * Abstract {@link Mapping} that unwraps {@link ProtectedValue}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractProtectedValueMapping<D> implements Mapping<Object, D> {

	/** The access right that is needed to be able to see content of the {@link ProtectedValue}. */
	protected final BoundCommandGroup _requiredRight;

	/**
	 * Creates a new {@link AbstractProtectedValueMapping}.
	 */
	public AbstractProtectedValueMapping(BoundCommandGroup requiredRight) {
		_requiredRight = requiredRight;
	}

	@Override
	public D map(Object input) {
		D output;
		if (input instanceof ProtectedValue) {
			ProtectedValue protectedValue = (ProtectedValue) input;
			if (protectedValue.hasAccessRight(_requiredRight)) {
				output = unwrapProtectedValue(protectedValue);
			} else {
				output = blockedValue(protectedValue);
			}
		} else {
			output = handleUnprotected(input);
		}
		return output;
	}

	/**
	 * Unwraps a {@link ProtectedValue} where the user has the right to see the value.
	 * 
	 * <p>
	 * This implementation just delegates the {@link ProtectedValue#getValue() protected value} to
	 * {@link #handleUnprotected(Object)}.
	 * </p>
	 * 
	 * @param value
	 *        The {@link ProtectedValue} given in {@link #map(Object)}
	 * 
	 * @return The unwrapped value. This is the return value of {@link #map(Object)}.
	 * 
	 * @see #handleUnprotected(Object)
	 */
	protected D unwrapProtectedValue(ProtectedValue value) {
		return handleUnprotected(value.getValue());
	}

	/**
	 * @param value
	 *        The {@link ProtectedValue} whose value must not be seen by the current user.
	 * 
	 * @return The value to use for a {@link ProtectedValue} whose value must not be displayed. This
	 *         is the return value of {@link #map(Object)}.
	 */
	protected abstract D blockedValue(ProtectedValue value);

	/**
	 * Fallback for {@link #map(Object) input}s that are not protected value,
	 * 
	 * @param input
	 *        The input of {@link #map(Object)} which is not a {@link ProtectedValue}.
	 * @return This is the return value of {@link #map(Object)}.
	 */
	protected abstract D handleUnprotected(Object input);

}

