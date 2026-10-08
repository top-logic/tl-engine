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
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AccessParentFunction} leading to the object a to-one reference of the delegating object
 * points to.
 *
 * @param reference
 *        The reference navigated forwards. Its owner is the type of the delegating objects.
 */
public record TargetRelation(TLReference reference) implements AccessParentFunction {

	@Override
	public TLObject resolve(TLObject object) {
		Object value = object.tValue(reference);
		return value instanceof TLObject parent ? parent : null;
	}

	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return reference.getType() instanceof TLClass target ? Set.of(target) : Set.of();
	}

	@Override
	public ResKey getLabel() {
		return ResKey.text(TLModelUtil.qualifiedName(reference));
	}

}
