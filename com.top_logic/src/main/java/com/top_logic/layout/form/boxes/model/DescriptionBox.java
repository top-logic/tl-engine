/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import java.util.Arrays;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.boxes.layout.HorizontalLayout;

/**
 * Special {@link AbstractCollectionBox} that consists of at most two content {@link Box}es layouted
 * horizontally (an optional description and a content box).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DescriptionBox extends AbstractCollectionBox implements ContentBox {

	private ContentBox _description;

	private ContentBox _content;

	/**
	 * The CSS class for the {@link #getContent()} box in forms.
	 */
	public static final String DEFAULT_CONTENT_CSS_CLASS = "content";

	/**
	 * The CSS class for the {@link #getDescription()} box in forms.
	 */
	public static final String DEFAULT_DESCRIPTION_CSS_CLASS = "label";

	/**
	 * Creates a {@link DescriptionBox}.
	 */
	public DescriptionBox(ContentBox content) {
		super();
		assert content != null : "Missing content box.";
		setContent(content);
	}

	/**
	 * Whether the (optional) description box is set.
	 * 
	 * @see #getDescription()
	 */
	public boolean hasDescription() {
		return _description != null;
	}

	/**
	 * The (optional) description box.
	 * 
	 * @return The description box, or <code>null</code>, if no description has been set.
	 */
	public ContentBox getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public void setDescription(ContentBox description) {
		_description = replace(_description, description);
	}

	/**
	 * The (main) content box.
	 */
	public ContentBox getContent() {
		return _content;
	}

	/**
	 * @see #getContent()
	 */
	public void setContent(ContentBox content) {
		_content = replace(_content, content);
	}

	@Override
	protected void localLayout() {
		if (_description != null) {
			HorizontalLayout.INSTANCE.layout(this, Arrays.<Box> asList(_description, _content));
		} else {
			setDimension(1, 1);
		}
	}

	@Override
	protected void enterContent(int x, int y, int availableColumns, int availableRows, Table<ContentBox> table) {
		Box description = _description;

		boolean hasDescriptionColumn = availableColumns > 1;
		if (hasDescriptionColumn) {
			if (description == null) {
				description = Boxes.contentBox();
				description.setWidth(DisplayDimension.ZERO_PERCENT);
			}

			description.enter(x, y, 1, getRows(), table);
			_content.enter(x + description.getColumns(), y, getColumns() - 1, getRows(), table);
		} else {
			_content.enter(x, y, getColumns(), getRows(), table);
		}
	}

	@Override
	public void setStyle(String style) {
		_content.setStyle(style);
	}

	@Override
	public void setInitialColumns(int columns) {
		_content.setInitialColumns(columns);
	}

	@Override
	public void setInitialRows(int rows) {
		_content.setInitialRows(rows);
	}

	@Override
	public HTMLFragment getContentRenderer() {
		return _content.getContentRenderer();
	}
}
