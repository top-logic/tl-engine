/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;
import java.util.Optional;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.tree.renderer.TreeCellRenderer;

/**
 * {@link TreeCellRenderer} for the "tree" column of a table with {@link CompareRowObject} row
 * objects.
 * 
 * @see CompareRowTypeCellRenderer
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareTreeCellRenderer extends TreeCellRenderer {

	private CellRenderer _compareRenderer;

	/**
	 * Creates a {@link CompareTreeCellRenderer}.
	 * 
	 * <p>
	 * Note: Don't forget to call {@link #init(CompareService)} before using.
	 * </p>
	 */
	public CompareTreeCellRenderer(ResourceProvider resourceProvider, CellRenderer contentRenderer, int indentChars) {
		super(resourceProvider, contentRenderer, indentChars);
	}

	/**
	 * Completes the construction.
	 */
	public CompareTreeCellRenderer init(CompareService<? extends CompareInfo> compareService) {
		_compareRenderer = createCompareCellRenderer(compareService);
		return this;
	}

	/**
	 * {@link CompareCellRenderer}, which shall be used to render tree cell content.
	 */
	protected CompareCellRenderer<? extends CompareInfo> createCompareCellRenderer(
			CompareService<? extends CompareInfo> compareService) {
		return new CompareCellRenderer<>(newImageRenderer(), compareService, true);
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
				CompareTreeCellRenderer.this.writeTypeImage(context, out, cell);
			}

		};
	}

	@Override
	protected void writeDecorationContent(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		writeExpandButton(context, out, cell);
		_compareRenderer.writeCell(context, out, cell);
	}

	@Override
	protected void writeValueContent(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		Optional<CompareCell> unwrapCompareRow = createCompareCell(cell);
		if (unwrapCompareRow.isPresent()) {
			super.writeValueContent(context, out, unwrapCompareRow.get());
		}
	}

	/**
	 * {@link CompareCell}, if value content shall be rendered, {@link Optional#empty()}
	 *         otherwise.
	 */
	protected Optional<CompareCell> createCompareCell(Cell cell) {
		return Optional.of(new CompareCell(cell, true));
	}

	@Override
	protected void writeTypeImage(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		// Overriden to make accessible.
		super.writeTypeImage(context, out, cell);
	}

}

