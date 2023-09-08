/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen;

import java.util.stream.Stream;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * Description of a change to the application model.
 */
public interface ModelChangeEvent {

	/**
	 * Description of the change to a single object.
	 */
	enum ChangeType {
		/**
		 * The object was created.
		 */
		CREATED,

		/**
		 * Properties of the object were updated.
		 */
		UPDATED,

		/**
		 * The object was deleted.
		 */
		DELETED,

		/**
		 * The object was not changed.
		 * <p>
		 * This is used if the object in question was not part of the {@link ModelChangeEvent}.
		 * </p>
		 */
		NONE;

	}

	/**
	 * Tests a given object for a change.
	 * 
	 * @return The kind of change performed to the given object.
	 */
	ChangeType getChange(TLObject existingObject);

	/**
	 * All objects that were internally modified.
	 */
	Stream<? extends TLObject> getUpdated();

	/**
	 * All objects of the given type that were internally modified.
	 */
	Stream<? extends TLObject> getUpdated(TLStructuredType type);

	/**
	 * All objects that were created.
	 */
	Stream<? extends TLObject> getCreated();

	/**
	 * All objects of the given type that were created.
	 */
	Stream<? extends TLObject> getCreated(TLStructuredType type);

	/**
	 * All objects that were deleted.
	 */
	Stream<? extends TLObject> getDeleted();

	/**
	 * All objects of the given type that were deleted.
	 */
	Stream<? extends TLObject> getDeleted(TLStructuredType type);

}
