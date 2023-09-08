/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.wml.Tc;


/**
 * Visits {@link Tc} elements and can return the list of them.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXFindTableCells extends DOCXVisitor {

	private List<Tc> _cells;

	/** Returns a {@link List} of the found {@link Tc} elements */
	public List<Tc> getCells() {
		return (_cells);
	}

	@Override
	protected boolean onVisit(Tc element) {
		if (_cells == null) {
			_cells = new ArrayList<>();
		}

		_cells.add(element);

		return true;
	}
}