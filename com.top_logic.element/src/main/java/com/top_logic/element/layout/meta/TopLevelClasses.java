/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.col.Filter;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link Filter} that accepts {@link TLClass}es not being derived from other types in their own
 * module.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TopLevelClasses implements Filter<TLType> {

	/**
	 * Singleton {@link TopLevelClasses} instance.
	 */
	public static final TopLevelClasses INSTANCE = new TopLevelClasses();

	private TopLevelClasses() {
		// Singleton constructor.
	}

	@Override
	public boolean accept(TLType type) {
		if (type.getModelKind() != ModelKind.CLASS) {
			return false;
		}
		if (TLModelUtil.hasGeneralizationsInSameModule((TLClass) type)) {
			return false;
		}
		return true;
	}

}