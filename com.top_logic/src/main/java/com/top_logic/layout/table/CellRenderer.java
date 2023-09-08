/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.control.TableControl;

/**
 * A {@link CellRenderer} renders the individual cell contents of a
 * {@link TableControl}.
 * 
 * <p>
 * A {@link CellRenderer} is not a {@link Renderer} implementation, because it
 * requires more context information than the actual value in the rendered
 * table's cell.
 * </p>
 * 
 * <p>
 * Instead of directly implementing this interface, {@link AbstractCellRenderer} should be extended
 * for compatibility with future changes.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CellRenderer {

	/**
	 * Write the cell contents of the specified cell.
	 * 
	 * @param context
	 *        The context in which the rendering occurs.
	 * @param out
	 *        The writer to write the generated contents to.
	 * @param cell
	 *        Description of the currently rendered cell.
	 */
	void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException;

}
