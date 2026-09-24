/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * The references a type can name as its
 * {@link SecurityConfigurationService.TLClassAccessRights#getAccessParent() access parent}: the
 * to-one references of the type and the compositions holding objects of the type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AccessParentOptions extends Function1<Collection<TLReference>, String> {

	@Override
	public Collection<TLReference> apply(String typeName) {
		if (StringServices.isEmpty(typeName)) {
			return Collections.emptyList();
		}
		TLType type;
		try {
			type = TLModelUtil.findType(ModelService.getApplicationModel(), typeName);
		} catch (RuntimeException ex) {
			// The name is not a type of the application: nothing to offer.
			return Collections.emptyList();
		}
		if (!(type instanceof TLClass clazz)) {
			return Collections.emptyList();
		}
		List<TLReference> result = new ArrayList<>();
		for (TLStructuredTypePart part : clazz.getAllParts()) {
			if (part instanceof TLReference reference && !reference.isMultiple()) {
				result.add(reference);
			}
		}
		for (TLClass owner : TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel())) {
			for (TLStructuredTypePart part : owner.getLocalParts()) {
				if (part instanceof TLReference reference && reference.isComposite()
					&& TLModelUtil.isCompatibleType(reference.getType(), clazz) && !result.contains(reference)) {
					result.add(reference);
				}
			}
		}
		result.sort(Comparator.comparing(TLModelUtil::qualifiedName));
		return result;
	}

}
