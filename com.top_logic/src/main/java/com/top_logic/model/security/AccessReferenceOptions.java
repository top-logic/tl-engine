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
import com.top_logic.basic.func.Function2;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * The references a type can name as its
 * {@link SecurityConfigurationService.TLClassAccessRights#getAccessReference() access reference}
 * for the chosen {@link SecurityConfigurationService.TLClassAccessRights#getAccessParent() kind of
 * access parent}: the compositions holding objects of the type for {@link AccessParentKind#CONTAINER},
 * the to-one references of the type for {@link AccessParentKind#TARGET}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AccessReferenceOptions extends Function2<Collection<TLReference>, String, AccessParentKind> {

	@Override
	public Collection<TLReference> apply(String typeName, AccessParentKind kind) {
		if (StringServices.isEmpty(typeName) || kind == null || !kind.delegates()) {
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
		List<TLReference> result = kind == AccessParentKind.CONTAINER ? compositions(clazz) : toOneReferences(clazz);
		result.sort(Comparator.comparing(TLModelUtil::qualifiedName));
		return result;
	}

	/**
	 * The to-one references of the given type, which can be navigated forwards.
	 */
	private static List<TLReference> toOneReferences(TLClass type) {
		List<TLReference> result = new ArrayList<>();
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part instanceof TLReference reference && !reference.isMultiple()) {
				result.add(reference);
			}
		}
		return result;
	}

	/**
	 * The compositions holding objects of the given type, which can be navigated backwards.
	 */
	private static List<TLReference> compositions(TLClass type) {
		List<TLReference> result = new ArrayList<>();
		for (TLClass owner : TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel())) {
			for (TLStructuredTypePart part : owner.getLocalParts()) {
				if (part instanceof TLReference reference && reference.isComposite()
					&& TLModelUtil.isCompatibleType(reference.getType(), type)) {
					result.add(reference);
				}
			}
		}
		return result;
	}

}
