/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import java.util.HashMap;
import java.util.Map;

import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;

import com.top_logic.basic.StringServices;

/**
 * Visits all {@link Tbl} elements and stores those {@link Tbl}s that contain a value their first
 * {@link Text}-element.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXMapTables extends DOCXVisitor {

	/** @see #getTables() */
	Map<String, Tbl> _tables;

	@Override
	protected boolean onVisit(Tbl element) {

		if (_tables == null) {
			_tables = new HashMap<>();
		}

		DOCXFindFirstText visitor = new DOCXFindFirstText();
		DOCXVisitor.visit(element, visitor);
		Text text = visitor.getText();

		if (text != null) {
			String tableName = text.getValue();
			if (!StringServices.isEmpty(tableName)) {
				_tables.put(tableName, element);
			}
		}

		return true;
	}

	/**
	 * The {@link Tbl}s that contain a {@link Text}
	 */
	public Map<String, Tbl> getTables() {
		return (_tables);
	}
}