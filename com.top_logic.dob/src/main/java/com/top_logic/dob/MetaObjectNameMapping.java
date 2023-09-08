/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.basic.col.Mapping;

/**
 * {@link Mapping} that provides the {@link MetaObject#getName()} for a
 * {@link MetaObject}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class MetaObjectNameMapping implements Mapping {

	/**
	 * Singleton instance of {@link MetaObjectNameMapping}.
	 */
	public static final Mapping INSTANCE = new MetaObjectNameMapping();
	
	private MetaObjectNameMapping() {
		// Singleton constructor.
	}
	
	@Override
	public Object map(Object input) {
		return ((MetaObject) input).getName();
	}

}
