/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.tree.renderer.RowTypeCellRenderer;

/**
 * {@link RowTypeCellRenderer} for the "row type" column of a table with {@link CompareRowObject}
 * row objects.
 * 
 * @see CompareTreeCellRenderer
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareRowTypeCellRenderer extends RowTypeCellRenderer {

	private CellRenderer _compareRenderer;

	/**
	 * Creates a {@link CompareRowTypeCellRenderer}.
	 * 
	 * <p>
	 * Note: Don't forget to call {@link #init(CompareService)} before using.
	 * </p>
	 */
	public CompareRowTypeCellRenderer(ResourceProvider resourceProvider, CellRenderer contentRenderer) {
		super(resourceProvider, contentRenderer);
	}

	/**
	 * Finishes construction.
	 */
	public CompareRowTypeCellRenderer init(CompareService<? extends CompareInfo> compareService) {
		// Note: The overridden method createCompareCellRenderer() cannot be called from the
		// constructor, since the overriding implementation depends on value set in its constructor.
		_compareRenderer = createCompareCellRenderer(compareService);
		return this;
	}

	/**
	 * {@link CompareCellRenderer}, that renders the tree cell value.
	 */
	protected CompareCellRenderer<? extends CompareInfo> createCompareCellRenderer(
			CompareService<? extends CompareInfo> compareService) {
		return new CompareCellRenderer<>(newImageRenderer(), compareService, false);
	}

	/**
	 * {@link CellRenderer} that renders the type image.
	 *
	 * @see #writeTypeImage(DisplayContext, TagWriter, Cell)
	 */
	protected CellRenderer newImageRenderer() {
		return new AbstractCellRenderer() {

			@Override
			public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
				CompareRowTypeCellRenderer.this.writeTypeImage(context, out, cell);
			}

		};
	}

	@Override
	protected void writeDecorationContent(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		_compareRenderer.writeCell(context, out, cell);
	}

	@Override
	protected void writeValueContent(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		CompareCell unwrapCompareRow = new CompareCell(cell, false);
		super.writeValueContent(context, out, unwrapCompareRow);
	}

	void writeTypeImage(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		super.writeDecorationContent(context, out, cell);
	}

}

