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
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Option functions for the reference of an {@link AccessParentDefinition}, offering the references
 * fitting the type of the enclosing {@link TLClassAccessRights} entry.
 */
public class AccessParentReferenceOptions {

	/**
	 * The compositions holding objects of the named type, which can be navigated backwards, see
	 * {@link ContainerAccessParent}.
	 */
	public static class Compositions extends Function1<Collection<TLReference>, String> {

		@Override
		public Collection<TLReference> apply(String typeName) {
			TLClass type = type(typeName);
			return type == null ? Collections.emptyList() : sorted(compositions(type));
		}

	}

	/**
	 * The to-one references of the named type, which can be navigated forwards, see
	 * {@link TargetAccessParent}.
	 */
	public static class ToOneReferences extends Function1<Collection<TLReference>, String> {

		@Override
		public Collection<TLReference> apply(String typeName) {
			TLClass type = type(typeName);
			return type == null ? Collections.emptyList() : sorted(toOneReferences(type));
		}

	}

	/**
	 * The configured reference of an {@link AccessParentDefinition}, resolved against the
	 * application model.
	 *
	 * @param context
	 *        The context to report a reference that does not exist to.
	 * @param ref
	 *        The configured reference.
	 * @param typeName
	 *        The name of the type the definition is configured for, for the error message.
	 * @return <code>null</code> when the reference does not exist, which is reported.
	 */
	static TLModelPart resolve(InstantiationContext context, TLModelPartRef ref, String typeName) {
		try {
			return ref.resolve(ModelService.getApplicationModel());
		} catch (RuntimeException ex) {
			context.error("The access reference " + ref + " of " + typeName + " does not exist.", ex);
			return null;
		}
	}

	/**
	 * The type with the given name, <code>null</code> when the name is no class of the application.
	 */
	private static TLClass type(String typeName) {
		if (StringServices.isEmpty(typeName)) {
			return null;
		}
		try {
			return TLModelUtil.findType(ModelService.getApplicationModel(), typeName) instanceof TLClass clazz
				? clazz
				: null;
		} catch (RuntimeException ex) {
			// The name is not a type of the application: nothing to offer.
			return null;
		}
	}

	private static List<TLReference> sorted(List<TLReference> references) {
		references.sort(Comparator.comparing(TLModelUtil::qualifiedName));
		return references;
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
