/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.wml.Tr;


/**
 * Visits {@link Tr} elements and can return a list of them.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXFindTableRows extends DOCXVisitor {

	private List<Tr> _rows;

	/** Returns a {@link List} of the found {@link Tr} elements */
	public List<Tr> getRows() {
		return (_rows);
	}

	@Override
	protected boolean onVisit(Tr element) {
		if (_rows == null) {
			_rows = new ArrayList<>();
		}

		_rows.add(element);

		return true;
	}
}