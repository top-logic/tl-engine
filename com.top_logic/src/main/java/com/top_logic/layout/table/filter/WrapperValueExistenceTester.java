/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * {@link CellExistenceTester}, that checks, whether a wrapper can deliver a value for given
 * attribute, or not.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class WrapperValueExistenceTester implements CellExistenceTester {

	/** Static instance of {@link WrapperValueExistenceTester} */
	public static final CellExistenceTester INSTANCE = new WrapperValueExistenceTester();

	private static final String NAME_ATTRIBUTE = "name";

	private WrapperValueExistenceTester() {
		// Singleton instance
	}

	@Override
	public boolean isCellExistent(Object rowObject, String columnName) {
		if (!(rowObject instanceof TLObject)) {
			return false;
		}
		if (NAME_ATTRIBUTE.equals(columnName)) {
			// All wrapper have name attributes (but they must not be set necessarily)
			return true;
		}
		if (WrapperAccessor.IDENTIFIER_PROPERTY.equals(columnName)) {
			return true;
		}

		TLClass rowType = (TLClass) ((TLObject) rowObject).tType();
		return rowType.getPart(columnName) != null;
	}
}
