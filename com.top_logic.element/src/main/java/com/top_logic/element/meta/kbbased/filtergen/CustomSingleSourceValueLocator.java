/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collection;

/**
 * Common base class for custom {@link AttributeValueLocator}s deriving their value from a singel
 * source object (not a {@link Collection}).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CustomSingleSourceValueLocator extends AbstractSingleSourceValueLocator {

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
