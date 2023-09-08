/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellAdapter;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * {@link CellRenderer} that unwraps {@link ProtectedValue} for user that have the right to see its
 * value, and writes a replacement string in case the user have not enough access rights.
 * 
 * @see ProtectedValueRenderer
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ProtectedValueCellRenderer extends AbstractCellRenderer {

	private final CellRenderer _dispatch;

	private BoundCommandGroup _requiredRight;

	/**
	 * Creates a new {@link ProtectedValueCellRenderer}.
	 * 
	 * @param dispatch
	 *        The {@link CellRenderer} to dispatch unwrapped cell to.
	 * @param requestedRight
	 *        Theright a user must have to see the value of a {@link ProtectedValue}.
	 */
	public ProtectedValueCellRenderer(CellRenderer dispatch, BoundCommandGroup requestedRight) {
		_dispatch = dispatch;
		_requiredRight = requestedRight;
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		Object value = cell.getValue();
		if (value instanceof ProtectedValue) {
			ProtectedValue protectedValue = (ProtectedValue) value;
			if (protectedValue.hasAccessRight(_requiredRight)) {
				_dispatch.writeCell(context, out, unprotectCell(cell));
			} else {
				ProtectedValueRenderer.writeBlockedContent(out, context);
			}
		} else {
			_dispatch.writeCell(context, out, cell);
		}
	}

	private Cell unprotectCell(final Cell cell) {
		return new CellAdapter() {

			@Override
			public Object getValue() {
				return ((ProtectedValue) super.getValue()).getValue();
			}

			@Override
			protected Cell impl() {
				return cell;
			}
		};
	}

}

