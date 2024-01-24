/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dnd;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.Control;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * API for a {@link Control} that might operate as source of a drag-and-drop operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DragSourceSPI {

	/**
	 * The source model from which the drag data is taken from.
	 * 
	 * @see DropEvent#getSource()
	 */
	Object getDragSourceModel();

	/**
	 * Resolves the client-side representation of the drag data to an element of the
	 * {@link #getDragSourceModel()}.
	 * 
	 * @param ref
	 *        Client-side representation of the drag data.
	 * @return Server-side drag data, see {@link DropEvent#getData()}.
	 */
	Object getDragData(String ref);

	/**
	 * Tries to create a {@link ModelName} for the object with the given client-side identifier.
	 * 
	 * @param dragSource
	 *        The {@link #getDragSourceModel() source model}
	 * @param ref
	 *        Client-side representation of the drag data.
	 * 
	 * @return {@link ModelName} for the drag data or empty if no such name could be created.
	 */
	Maybe<? extends ModelName> getDragDataName(Object dragSource, String ref);

}
