/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;

/**
 * Option provider delivering all available {@link TLClass}es.
 * 
 * @see Options#fun()
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllClasses extends AllTypes {

	@Override
	protected TypesTree tree() {
		return new ClassesTree();
	}

	/**
	 * Tree model of {@link TLClass} options.
	 * 
	 * @see com.top_logic.model.util.AllTypes.TypesTree
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	protected static class ClassesTree extends TypesTree {

		@Override
		protected boolean acceptType(TLModule module, TLType type) {
			ModelKind kind = type.getModelKind();

			if (kind != ModelKind.CLASS) {
				return false;
			}

			return super.acceptType(module, type);
		}
	}
}