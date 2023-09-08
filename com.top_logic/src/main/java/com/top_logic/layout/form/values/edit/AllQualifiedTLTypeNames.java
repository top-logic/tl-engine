/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.func.Function0;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Provides all non-empty global Qualified {@link TLType} names.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllQualifiedTLTypeNames extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		return getFilteredGlobalTypes()
			.stream()
			.map(tlType -> TLModelUtil.qualifiedName(tlType))
			.filter(qualifiedName -> !StringServices.isEmpty(qualifiedName))
			.collect(Collectors.toList());
	}

	private List<TLType> getFilteredGlobalTypes() {
		Collection<TLClass> allGlobalClasses = TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel());

		return ModelService.getInstance().filterModel(allGlobalClasses);
	}
}
