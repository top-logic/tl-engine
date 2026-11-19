/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.model.Drawable;
import com.top_logic.react.flow.data.Widget;

/**
 * Custom operations defined for a {@link Widget}.
 */
public interface WidgetOperations extends Drawable, MapLike {

	@Override
	Widget self();

	/**
	 * Whether a drop on the diagram is made onto this element, as its definition says: whether it
	 * has a {@link Widget#getUserObject() user object}, which is then the object the drop is made
	 * onto.
	 *
	 * <p>
	 * The value the element transmits to the client as {@link Widget#isDropTarget()}.
	 * </p>
	 */
	default boolean isDropTargetByDefinition() {
		return self().getUserObject() != null;
	}

}
