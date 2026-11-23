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
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.model.ModelService;

/**
 * Option functions for the reference of an {@link AccessParentDefinition}, offering the references
 * fitting the type of the enclosing {@link TLClassAccessRights} entry, and for the operations it is
 * restricted to.
 *
 * <p>
 * Each function is given the {@link OperationRestriction#getOwner() owner} of the definition: the
 * {@link AccessParentConfig setting} of the entry, or a definition combining several ones like
 * {@link AnyOfAccessParent}, however deeply nested. The type is found by following the containers
 * up to the setting, see {@link #configuredType(ConfigurationItem)}.
 * </p>
 */
public class AccessParentReferenceOptions {

	/**
	 * The compositions holding objects of the configured type, which can be navigated backwards,
	 * see {@link ContainerAccessParent}.
	 */
	public static class Compositions extends Function1<Collection<TLReference>, ConfigurationItem> {

		@Override
		public Collection<TLReference> apply(ConfigurationItem owner) {
			TLClass type = configuredType(owner);
			return type == null ? Collections.emptyList() : sorted(compositions(type));
		}

	}

	/**
	 * The references of the configured type, which can be navigated forwards, see
	 * {@link TargetAccessParent}.
	 */
	public static class References extends Function1<Collection<TLReference>, ConfigurationItem> {

		@Override
		public Collection<TLReference> apply(ConfigurationItem owner) {
			TLClass type = configuredType(owner);
			return type == null ? Collections.emptyList() : sorted(references(type));
		}

	}

	/**
	 * The references of the application model pointing to objects of the configured type, which
	 * can be navigated backwards, see {@link RefererAccessParent}.
	 */
	public static class Referers extends Function1<Collection<TLReference>, ConfigurationItem> {

		@Override
		public Collection<TLReference> apply(ConfigurationItem owner) {
			TLClass type = configuredType(owner);
			return type == null ? Collections.emptyList() : sorted(referencesTo(type, false));
		}

	}

	/**
	 * The operations applying to the configured type, see {@link OperationRestriction}.
	 */
	public static class Operations extends Function1<Collection<BoundCommandGroup>, ConfigurationItem> {

		@Override
		public Collection<BoundCommandGroup> apply(ConfigurationItem owner) {
			TLClass type = configuredType(owner);
			return type == null ? Collections.emptyList()
				: ModelAccessRights.getInstance().getOperations(type).getOperations();
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
	 * The type the access parent setting enclosing the given owner of a definition is configured
	 * for.
	 *
	 * @param owner
	 *        The {@link OperationRestriction#getOwner() owner} of a definition, <code>null</code>
	 *        while the definition is not part of a setting.
	 * @return <code>null</code> when the owner is in no setting of an entry for a class of the
	 *         application.
	 */
	static TLClass configuredType(ConfigurationItem owner) {
		ConfigurationItem item = owner;
		while (item != null) {
			if (item instanceof AccessParentConfig setting) {
				TLClassAccessRights rights = setting.getRights();
				return rights == null ? null : type(rights.getName());
			}
			item = item instanceof ConfigPart part ? part.container() : null;
		}
		return null;
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
	 * The references of the given type, which can be navigated forwards.
	 */
	private static List<TLReference> references(TLClass type) {
		List<TLReference> result = new ArrayList<>();
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part instanceof TLReference reference) {
				result.add(reference);
			}
		}
		return result;
	}

	/**
	 * The compositions holding objects of the given type, which can be navigated backwards.
	 */
	private static List<TLReference> compositions(TLClass type) {
		return referencesTo(type, true);
	}

	/**
	 * The references of the application model pointing to objects of the given type, which can be
	 * navigated backwards.
	 *
	 * @param compositionsOnly
	 *        Whether only compositions are wanted.
	 */
	private static List<TLReference> referencesTo(TLClass type, boolean compositionsOnly) {
		List<TLReference> result = new ArrayList<>();
		for (TLClass owner : TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel())) {
			for (TLStructuredTypePart part : owner.getLocalParts()) {
				if (part instanceof TLReference reference && (!compositionsOnly || reference.isComposite())
					&& TLModelUtil.isCompatibleType(reference.getType(), type)) {
					result.add(reference);
				}
			}
		}
		return result;
	}

}
