/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.dnd;

import com.top_logic.layout.table.dnd.TableDropEvent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Handler for drop operations on generic {@link LayoutComponent}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ComponentDropTarget {

	/**
	 * Whether the given table accepts drop operations.
	 * 
	 * @param component
	 *        The potential drop target.
	 */
	default boolean dropEnabled(LayoutComponent component) {
		return true;
	}

	/**
	 * Announces a drop operation on a table.
	 * 
	 * @param event
	 *        Information further describing the drop details.
	 * 
	 * @see TableDropEvent#getTarget()
	 */
	void handleDrop(ComponentDropEvent event);

}
