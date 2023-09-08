/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

/**
 * Object typed by a static {@link #tTable()} type.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface StaticTyped {

	/**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tTable()} instead
	 */
	@Deprecated
	default MetaObject getMetaObject() {
		return tTable();
	}

	/**
	 * The {@link MetaObject} describing the structure of this instance.
	 * 
	 * @return The requested meta object.
	 */
	MetaObject tTable();

}
