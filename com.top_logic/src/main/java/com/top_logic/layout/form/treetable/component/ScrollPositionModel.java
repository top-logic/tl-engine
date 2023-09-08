/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import com.top_logic.layout.table.control.IndexViewportState;

/**
 * Model, that holds scrolling position, suitable for entities, structured by rows and columns (e.g.
 * tables)
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface ScrollPositionModel {

	/**
	 * table scroll position, stored in this model.
	 */
	IndexViewportState getScrollPosition();

	/**
	 * @see #getScrollPosition()
	 */
	void setScrollPosition(IndexViewportState scrollPosition);

}
