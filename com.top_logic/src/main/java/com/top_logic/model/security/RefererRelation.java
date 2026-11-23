/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * {@link AccessParentFunction} leading to the objects pointing to the delegating object through a
 * reference, see {@link RefererAccessParent}.
 *
 * <p>
 * Each referring object is an access parent of its own. Only committed referring objects with
 * access control of their own count, see
 * {@link AccessParentFunction#givesVerdict(TLObject, TLObject, Object)}: an object created in the
 * current transaction is set up by whoever creates it, without any check on the object it refers
 * to, and therefore gives no verdict for that object; an object that cannot hold roles gives no
 * verdict either, and is logged as an error.
 * </p>
 *
 * @param reference
 *        The reference navigated backwards. Its target type is compatible with the type of the
 *        delegating objects.
 */
public record RefererRelation(TLReference reference) implements AccessParentFunction {

	/**
	 * @return The first object pointing to the given one that gives a verdict on it.
	 */
	@Override
	public TLObject resolve(TLObject object) {
		for (TLObject referer : object.tReferers(reference)) {
			if (AccessParentFunction.givesVerdict(referer, object, description())) {
				return referer;
			}
		}
		return null;
	}

	@Override
	public Iterable<TLObject> resolveAll(TLObject object, BoundCommandGroup operation) {
		if (!delegates(operation)) {
			return List.of();
		}
		return () -> object.tReferers(reference).stream()
			.filter(referer -> AccessParentFunction.givesVerdict(referer, object, description()))
			.map(TLObject.class::cast)
			.iterator();
	}

	/**
	 * The definition of this relation, for the log.
	 */
	private String description() {
		return "<referer reference=\"" + TLModelUtil.qualifiedName(reference) + "\"/>";
	}

	/**
	 * @return The owner of the reference.
	 */
	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return reference.getOwner() instanceof TLClass owner ? Set.of(owner) : Set.of();
	}

	@Override
	public ResKey getLabel() {
		return I18NConstants.ACCESS_PARENT_REFERER__OWNER_REFERENCE.fill(AccessParentFunction.ownerLabel(reference),
			AccessParentFunction.referenceLabel(reference));
	}

}
