/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

/**
 * Common base class for custom {@link AttributeValueLocator}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CustomAttributeValueLocator extends AbstractAttributeValueLocator {

	@Override
	protected String getValueTypeSpec() {
		return ANY_TYPE;
	}

	@Override
	protected final boolean isBackReference() {
		return false;
	}

	@Override
	protected final boolean isCollection() {
		return false;
	}

	@Override
	protected final String getReverseEndSpec() {
		return null;
	}

}
