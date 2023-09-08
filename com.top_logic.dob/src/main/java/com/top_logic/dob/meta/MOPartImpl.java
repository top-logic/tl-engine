/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOPart;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;

/**
 * The {@link MOPartImpl} is the default implementation of {@link MOPart}.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MOPartImpl implements MOPart {

	/**
	 * States that the {@link MOPartImpl} is currently modifiable.
	 * 
	 * @see MOPartImpl#_state
	 */
	private static final byte MODIFIABLE = 0;

	/**
	 * States that the {@link MOPartImpl} currently {@link #freeze()} is run.
	 * 
	 * @see MOPartImpl#_state
	 */
	private static final byte FREEZING = 1;

	/**
	 * States that the {@link MOPartImpl} has successfully been frozen.
	 * 
	 * @see MOPartImpl#_state
	 */
	private static final byte FROZEN = 2;

	/**
	 * The name of this {@link MOPart}
	 */
	private final String name;
	
	/**
	 * Modifiable state of this {@link MOPart}.
	 * 
	 * @see MOPartImpl#MODIFIABLE
	 * @see MOPartImpl#FREEZING
	 * @see MOPartImpl#FROZEN
	 */
	private byte _state = MODIFIABLE;

	public MOPartImpl(String name) {
		super();
		this.name = name;
	}

	/**
	 * Returns the name of this MetaObject
	 */
	@Override
	public final String getName() {
	    return name;
	}

	@Override
	public final void freeze() {
		if (_state != MODIFIABLE) {
			return;
		}
	
		this._state = FREEZING;

		try {
			this.afterFreeze();
			_state = FROZEN;
		} finally {
			if (!isFrozen()) {
				_state = MODIFIABLE;
			}
		}
	}

	/**
	 * Hook for subclasses to optimize internal data structures for fast access
	 * after this instance becomes immutable in a call to {@link #freeze()}.
	 */
	protected void afterFreeze() {
		// Nothing to do.
	}

	@Override
	public final boolean isFrozen() {
		return _state == FROZEN;
	}

	protected final void checkUpdate() {
		if (isFrozen()) {
			throw new IllegalStateException("Frozen parts may no longer be updated.");
		}
	}


	/**
	 * Replaces the given type with a type reference.
	 * 
	 * <p>
	 * Utility for implementing {@link MetaObject#copy()}.
	 * </p>
	 */
	protected static MetaObject typeRef(MetaObject type) {
		if (type == null) {
			return null;
		}
		if (type instanceof MOPrimitive) {
			return type;
		} else {
			return new DeferredMetaObject(type.getName());
		}
	}

	/**
	 * Replaces the given types with type references.
	 * 
	 * <p>
	 * Utility for implementing {@link MetaObject#copy()}.
	 * </p>
	 */
	protected static List<MetaObject> typesRef(List<MetaObject> types) {
		List<MetaObject> resolvedArgumentTypes = new ArrayList<>(types.size());
		for (MetaObject argumentType : types) {
			resolvedArgumentTypes.add(typeRef(argumentType));
		}
		return resolvedArgumentTypes;
	}

	/**
	 * In-place {@link MetaObject#resolve(TypeContext)} all types in the given list.
	 * @throws DataObjectException TODO
	 */
    protected static void resolveTypes(TypeContext context, List<MetaObject> types) throws DataObjectException {
		for (int n = 0, cnt = types.size(); n < cnt; n++) {
			types.set(n, types.get(n).resolve(context));
		}
	}

}
