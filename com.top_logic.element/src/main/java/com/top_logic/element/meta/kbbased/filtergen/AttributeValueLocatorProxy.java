/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Set;

import com.top_logic.model.TLObject;

/**
 * Proxy for an {@link AbstractAttributeValueLocator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AttributeValueLocatorProxy extends AbstractAttributeValueLocator {

	@Override
	public Object locateAttributeValue(Object anObject) {
		return impl().locateAttributeValue(anObject);
	}

	@Override
	protected String getValueTypeSpec() {
		return impl().getValueTypeSpec();
	}

	@Override
	protected boolean isBackReference() {
		return impl().isBackReference();
	}

	@Override
	protected boolean isCollection() {
		return impl().isCollection();
	}

	@Override
	protected String getReverseEndSpec() {
		return impl().getReverseEndSpec();
	}

	@Override
	public Set<? extends TLObject> locateReferers(Object value) {
		return impl().locateReferers(value);
	}

	/**
	 * The underlying implementation to dispatch to.
	 */
	protected abstract AbstractAttributeValueLocator impl();

}
