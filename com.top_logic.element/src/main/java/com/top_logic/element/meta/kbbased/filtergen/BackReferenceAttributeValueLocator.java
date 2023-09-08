/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

/**
 * Common base class of {@link AttributeValueLocator}s implementing back references.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class BackReferenceAttributeValueLocator extends AbstractSingleSourceValueLocator {

	@Override
	protected final boolean isBackReference() {
		return true;
	}

}
