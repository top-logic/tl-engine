/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * A model reference to a certain value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Pointer {

	/**
	 * The {@link TLObject} that stores the referenced value.
	 */
	TLObject object();

	/**
	 * The attribute of the {@link #object()} that stores the referenced value.
	 */
	TLStructuredTypePart attribute();

	/**
	 * Retrieves the value of the {@link Pointer}.
	 */
	default Object getValue() {
		return object().tValue(attribute());
	}

	/**
	 * Updates the value of the {@link Pointer} to the given new value.
	 */
	default void setValue(Object value) {
		object().tUpdate(attribute(), value);
	}

	/**
	 * Creates a {@link Pointer} to the value of the given attribute in the given object.
	 *
	 * @param object
	 *        See {@link #object()}.
	 * @param attribute
	 *        See {@link #attribute()}.
	 * @return A new {@link Pointer} to the given concrete value.
	 */
	static Pointer create(TLObject object, TLStructuredTypePart attribute) {
		return new Pointer() {
			@Override
			public TLObject object() {
				return object;
			}

			@Override
			public TLStructuredTypePart attribute() {
				return attribute;
			}

			@Override
			public int hashCode() {
				return object().hashCode() + 69763 * attribute().hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == this) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (obj.getClass() != getClass()) {
					return false;
				}
				Pointer other = (Pointer) obj;
				return object() == other.object() && attribute() == other.attribute();
			}
		};
	}

}