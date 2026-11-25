/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * {@link AccessParentFunction} leading to the objects a reference of the delegating object points
 * to.
 *
 * <p>
 * Each object a multiple reference points to is an access parent of its own.
 * </p>
 *
 * @param reference
 *        The reference navigated forwards. Its owner is the type of the delegating objects.
 */
public record TargetRelation(TLReference reference) implements AccessParentFunction {

	/**
	 * @return The object the reference points to, the first one for a multiple reference.
	 */
	@Override
	public TLObject resolve(TLObject object) {
		Object value = object.tValue(reference);
		if (value instanceof Collection<?> values) {
			for (Object element : values) {
				if (element instanceof TLObject parent) {
					return parent;
				}
			}
			return null;
		}
		return value instanceof TLObject parent ? parent : null;
	}

	@Override
	public Iterable<TLObject> resolveAll(TLObject object, BoundCommandGroup operation) {
		if (!delegates(operation)) {
			return List.of();
		}
		Object value = object.tValue(reference);
		if (value instanceof Collection<?> values) {
			List<TLObject> result = new ArrayList<>(values.size());
			for (Object element : values) {
				if (element instanceof TLObject parent) {
					result.add(parent);
				}
			}
			return result;
		}
		return value instanceof TLObject parent ? List.of(parent) : List.of();
	}

	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return reference.getType() instanceof TLClass target ? Set.of(target) : Set.of();
	}

	@Override
	public ResKey getLabel() {
		return I18NConstants.ACCESS_PARENT_TARGET__OWNER_REFERENCE.fill(AccessParentFunction.ownerLabel(reference),
			AccessParentFunction.referenceLabel(reference));
	}

}
