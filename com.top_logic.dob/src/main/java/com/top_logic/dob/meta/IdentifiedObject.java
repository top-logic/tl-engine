/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.identifier.ObjectKey;

/**
 * Object which is uniquely identified by some {@link ObjectKey}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface IdentifiedObject {

	/**
	 * {@link Mapping} to {@link #tId()} of the argument object.
	 */
	public static final Mapping<Object, ObjectKey> KEY_MAPPING = new Mapping<>() {

		@Override
		public ObjectKey map(Object object) {
			return ((IdentifiedObject) object).tId();
		}
	
		@Override
		public String toString() {
			return getClass().getName() + "[IdentifiedObject->ObjectKey]";
		}
	};

	/**
	 * The {@link ObjectKey} which uniquely identifies this persistent object.
	 * 
	 * @return The identifier for this object, or <code>null</code>, if this is a transient object.
	 */
	ObjectKey tId();

	/**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tId()} instead
	 */
	@Deprecated
	default ObjectKey getObjectKey() {
		return tId();
	}

}

