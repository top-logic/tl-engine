/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.func.Function0;
import com.top_logic.element.config.annotation.TLVisibleColumns;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.table.provider.AllColumnOptions;
import com.top_logic.layout.table.provider.ColumnOption;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Provides the column options for a type given by its form model.
 * 
 * @see TLVisibleColumns
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllVisibleColumns extends Function0<Collection<ColumnOption>> {

	TLModelPartRef _ref;

	/**
	 * Creates a {@link AllVisibleColumns}.
	 */
	@CalledByReflection
	public AllVisibleColumns(DeclarativeFormOptions options) {
		TypeRef typeReference = (TypeRef) options.get(DeclarativeFormBuilder.FORM_MODEL);

		_ref = TLModelPartRef.ref(typeReference.getTypeSpec());
	}

	@Override
	public Collection<ColumnOption> apply() {
		return AllColumnOptions.INSTANCE.apply(Collections.singleton(_ref));
	}

}
