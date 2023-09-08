/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.func.Function2;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.ui.model.operator.TypeCheck;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Option provider for {@link TypeCheck#getTypeCast()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TypeCastOptions extends Function2<List<TLClass>, TLType, String> {

	/** The {@link TypeCastOptions} instance. */
	public static final TypeCastOptions INSTANCE = new TypeCastOptions();

	@Override
	public List<TLClass> apply(TLType contextType, String configName) {
		if (containsNull(contextType, configName)) {
			return emptyList();
		}
		if (!(contextType instanceof TLClass)) {
			return emptyList();
		}
		Set<TLClass> specializations = TLModelUtil.getTransitiveSpecializations((TLClass) contextType);
		return ModelBasedSearch.getInstance().filterModel(specializations, configName);
	}

}
