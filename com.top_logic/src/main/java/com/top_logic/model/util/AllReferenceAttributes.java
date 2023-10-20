/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link AllTypeAttributes} returning all {@link TLReference} of a given {@link TLClass}.
 */
public class AllReferenceAttributes extends AllTypeAttributes {

	@Override
	protected List<TLStructuredTypePart> findAttributes(TLClass type) {
		return type.getAllParts().stream().filter(TLModelUtil::isReference).collect(Collectors.toList());
	}

}
