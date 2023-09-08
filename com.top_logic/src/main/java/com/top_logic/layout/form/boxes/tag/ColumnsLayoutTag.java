/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import com.top_logic.layout.form.boxes.border.HSpaceProvider;
import com.top_logic.layout.form.boxes.layout.ColumnsLayout;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;

/**
 * {@link BoxTag} crating a containers with {@link ColumnsLayout} with a configurable number of
 * columns.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColumnsLayoutTag extends AbstractBoxStructureTag {

	/**
	 * XML name of this tag.
	 */
	public static final String COLUMNS_LAYOUT_TAG = "form:columns";

	private static final int COUNT_DEFAULT = 2;

	private int _count = COUNT_DEFAULT;

	/**
	 * The maximum number of columns to use.
	 */
	public void setCount(int count) {
		_count = count;
	}

	@Override
	protected DefaultCollectionBox createCollectionBox() {
		return new DefaultCollectionBox(new ColumnsLayout(_count, HSpaceProvider.INSTANCE));
	}

	@Override
	protected String getTagName() {
		return COLUMNS_LAYOUT_TAG;
	}

	@Override
	protected void tearDown() {
		_count = COUNT_DEFAULT;

		super.tearDown();
	}
}
