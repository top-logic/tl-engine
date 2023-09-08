/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;

/**
 * Option provider delivering all available {@link TLEnumeration}es.
 * 
 * @see Options#fun()
 */
public class AllEnumerations extends AllTypes {

	@Override
	protected TypesTree tree() {
		return new EnumTree();
	}

	/**
	 * Tree model of {@link TLEnumeration} options.
	 * 
	 * @see com.top_logic.model.util.AllTypes.TypesTree
	 */
	protected static class EnumTree extends TypesTree {

		@Override
		protected boolean acceptType(TLModule module, TLType type) {
			ModelKind kind = type.getModelKind();

			if (kind != ModelKind.ENUMERATION) {
				return false;
			}

			return super.acceptType(module, type);
		}
	}
}