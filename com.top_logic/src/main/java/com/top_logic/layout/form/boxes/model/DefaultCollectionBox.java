/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.form.boxes.layout.BoxLayout;

/**
 * Default implementation of {@link AbstractCollectionBox}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultCollectionBox extends AbstractCollectionBox {

	private final BoxLayout _layout;

	private final List<Box> _boxes = new ArrayList<>();

	/**
	 * Creates a {@link DefaultCollectionBox}.
	 * 
	 * @param layout
	 *        See {@link #getLayout()}.
	 */
	public DefaultCollectionBox(BoxLayout layout) {
		_layout = layout;
	}

	/**
	 * Whether the content {@link Box}es are layed out horizontally (<code>false</code> for vertical
	 * layout).
	 */
	public BoxLayout getLayout() {
		return _layout;
	}

	/**
	 * Appends the given box to the {@link #getChildren()} of this box.
	 */
	public void addContent(Box content) {
		_boxes.add(replace(null, content));
	}

	@Override
	protected void localLayout() {
		_layout.layout(this, _boxes);
	}

	@Override
	protected void enterContent(int x, int y, int availableColumns, int availableRows, Table<ContentBox> table) {
		_layout.enter(this, table, x, y, _boxes);
	}

}
