/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.table.model.Column;

/**
 * {@link LabelProvider} for {@link Column}s that produces a structured label including all parent
 * column group names.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructuredColumnLabels implements LabelProvider {

	private final LabelProvider _directLabels;

	/**
	 * Creates a {@link StructuredColumnLabels}.
	 * 
	 * @param directLabels
	 *        {@link LabelProvider} that produce user-readable column names for technical column
	 *        names.
	 */
	public StructuredColumnLabels(LabelProvider directLabels) {
		_directLabels = directLabels;
	}

	@Override
	public String getLabel(Object object) {
		Column column = (Column) object;

		if (column.getParent() == null) {
			return directLabel(column);
		}

		StringBuilder buffer = new StringBuilder();
		printComposedLabel(buffer, column);
		return buffer.toString();
	}

	private boolean printComposedLabel(StringBuilder buffer, Column column) {
		Column parent = column.getParent();
		if (parent != null) {
			boolean parentHasLabel = printComposedLabel(buffer, parent);
			if (parentHasLabel) {
				buffer.append(": ");
			}
		}
		String label = directLabel(column);
		boolean hasLabel = !StringServices.isEmpty(label);
		if (hasLabel) {
			buffer.append(label);
		}
		return hasLabel;
	}

	private String directLabel(Column column) {
		return _directLabels.getLabel(column.getName());
	}
}