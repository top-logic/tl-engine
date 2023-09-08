/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CellRenderer} simply rendering the cell's value without decoration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultCellRenderer extends AbstractCellRenderer {

	/**
	 * Singleton {@link DefaultCellRenderer} instance.
	 */
	public static final DefaultCellRenderer INSTANCE = new DefaultCellRenderer();

	private DefaultCellRenderer() {
		// Singleton constructor.
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		// Use renderer that is wrapped into a CellRenderer if none is configured.
		Renderer<Object> fallbackRenderer = cell.getColumn().getConfig().finalRenderer();
		fallbackRenderer.write(context, out, cell.getValue());
	}

}
