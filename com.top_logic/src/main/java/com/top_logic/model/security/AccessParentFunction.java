/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelNamingConvention;

/**
 * The relation leading from an object to its access parent: the object whose access definition
 * decides access to it.
 * <p>
 * An object with an access parent has no grants and no roles of its own. Whether a user may
 * perform an operation on it is whether the user may perform the corresponding operation on its
 * access parent, and that parent may delegate further.
 * </p>
 *
 * @see ModelAccessRights#getAccessParent(TLClass)
 * @see AccessParentDefinition
 */
public interface AccessParentFunction {

	/**
	 * The access parent of the given object.
	 *
	 * @param object
	 *        The object delegating its access decision.
	 * @return The object deciding, <code>null</code> when the relation leads nowhere. An object
	 *         without access parent is not accessible.
	 */
	TLObject resolve(TLObject object);

	/**
	 * The types an access parent of an object of the given type may have.
	 *
	 * @param type
	 *        The delegating type.
	 * @return The possible types of the access parent, <code>null</code> when they are not known
	 *         statically. A type whose parent types are unknown counts as accessible in a check
	 *         that depends on no concrete object.
	 */
	Set<TLClass> getParentTypes(TLClass type);

	/**
	 * Short description of the relation for the coverage display.
	 */
	ResKey getLabel();

	/**
	 * The label of the type owning the given reference, its name where it has no label, for a
	 * {@link #getLabel() label} naming the reference.
	 */
	static ResKey ownerLabel(TLReference reference) {
		TLType owner = reference.getOwner();
		return ResKey.fallback(TLModelNamingConvention.getTypeLabelKey(owner), ResKey.text(owner.getName()));
	}

	/**
	 * The label of the given reference, its name where it has no label, for a
	 * {@link #getLabel() label} naming the reference.
	 */
	static ResKey referenceLabel(TLReference reference) {
		return ResKey.fallback(TLModelI18N.getI18nKeyWithoutFallback(reference), ResKey.text(reference.getName()));
	}

}
