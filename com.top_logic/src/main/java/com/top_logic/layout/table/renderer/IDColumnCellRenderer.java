/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.renderer.NoResourceProvider;
import com.top_logic.layout.tree.renderer.TreeCellRenderer;

/**
 * {@link CellRenderer} renders an expand and collapse button for tree like tables and uses the
 * {@link IDColumnTableCellRenderer} to render the cells value as link with an image of the row
 * objects type before.
 * 
 * @see IDColumnTableCellRenderer
 * @see TreeCellRenderer
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class IDColumnCellRenderer extends AbstractCellRenderer {

	private CellRenderer _cellRenderer;

	/**
	 * Creates a new {@link IDColumnCellRenderer}.
	 */
	public IDColumnCellRenderer(TableConfiguration tableConfig, ColumnConfiguration columnConfig) {
		_cellRenderer = createCellRenderer(tableConfig, columnConfig);
	}

	private CellRenderer createCellRenderer(TableConfiguration tableConfig, ColumnConfiguration columnConfig) {
		CellRenderer cellRenderer = createTableCellRenderer(tableConfig, columnConfig);

		if (tableConfig.isTree()) {
			cellRenderer = createWrappingTreeCellRenderer(cellRenderer);
		}

		return cellRenderer;
	}

	private CellRenderer createTableCellRenderer(TableConfiguration tableConfig, ColumnConfiguration columnConfig) {
		return new IDColumnTableCellRenderer(tableConfig, columnConfig);
	}

	private TreeCellRenderer createWrappingTreeCellRenderer(CellRenderer contentRenderer) {
		return new TreeCellRenderer(NoResourceProvider.INSTANCE, contentRenderer, TreeCellRenderer.DEFAULT_INDENT_CHARS);
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		_cellRenderer.writeCell(context, out, cell);
	}

}
