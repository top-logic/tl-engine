/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The {@link IdentityPathElement} is used for rules with no path elements to map some roles to
 * other roles.
 */
public class IdentityPathElement extends PathElement {

	/**
	 * Creates an {@link IdentityPathElement}.
	 */
    public IdentityPathElement() {
		super(null, false);
    }

	/**
	 * Returns a singleton collection containing {@code destination} itself, since the identity
	 * path element does not traverse any reference.
	 */
	@Override
	public Collection<? extends TLObject> getSources(TLObject destination) {
		return CollectionUtil.intoList(destination);
	}

	/**
	 * Returns a singleton collection containing {@code base} itself, since the identity path
	 * element does not traverse any reference.
	 */
	@Override
	public Collection<? extends TLObject> getValues(TLObject base) {
		return CollectionUtil.intoList(base);
	}

	/**
	 * Not supported: an {@link IdentityPathElement} has no underlying reference.
	 *
	 * @throws NullPointerException
	 *         always
	 */
	@Override
	public TLStructuredTypePart getMetaAttribute() {
		throw new NullPointerException("No MetaAttribute.");
	}

}
