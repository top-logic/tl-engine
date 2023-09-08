/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static java.util.Collections.*;

import java.util.Collection;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLClass;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Option provider delivering all {@link TLClass}es in the application.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllTypes extends Function1<Collection<TLClass>, String> {

	/** The {@link AllTypes} instance. */
	public static final AllTypes INSTANCE = new AllTypes();

	@Override
	public Collection<TLClass> apply(String configName) {
		if (configName == null) {
			return emptyList();
		}
		Collection<TLClass> allGlobalClasses = TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel());
		return ModelBasedSearch.getInstance().filterModel(allGlobalClasses, configName);
	}

}
