/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The {@link IdentityPathElement} is used for rules with no path elements to map some roles to
 * other roles.
 */
public class IdentityPathElement implements PathElement {

	/**
	 * Creates an {@link IdentityPathElement}.
	 */
    public IdentityPathElement() {
    }

	/**
	 * Returns a singleton collection containing {@code destination} itself, since the identity
	 * path element does not traverse any reference.
	 */
	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getSources(TLObject destination) {
		return BaseObjects.of(CollectionUtil.intoList(destination));
	}

	/**
	 * Returns a singleton collection containing {@code base} itself, since the identity path
	 * element does not traverse any reference.
	 */
	@Override
	public Collection<? extends TLObject> getValues(TLObject base) {
		return CollectionUtil.intoList(base);
	}

	@Override
	public Collection<TLStructuredTypePart> getRelevantParts() {
		return Collections.emptySet();
	}

	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getPathBase(TLObject element,
			TLStructuredTypePart part, Supplier<?> partValue) {
		return BaseObjects.of(CollectionUtil.intoList(element));
	}

	@Override
	public void appendId(Appendable out) throws IOException {
		// No ID for technical path element.
	}

	@Override
	public void appendForTooltip(Appendable out) throws IOException {
		// No tooltip for technical path element.
	}

}
