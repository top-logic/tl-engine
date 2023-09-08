/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.model.impl.generated.DerivedTLTypePartBase;

/**
 * {@link TLTypePart} which may be derived from other {@link TLTypePart}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DerivedTLTypePart extends DerivedTLTypePartBase {

	/**
	 * Whether this part can be computed from other parts.
	 * 
	 * <p>
	 * If a derived part has its own storage representation, this is a redundant cache for
	 * performance optimization. If this part has not an own storage representation, it is computed
	 * by the server from its source parts.
	 * </p>
	 */
	boolean isDerived();

	/**
	 * Sets the {@link DerivedTLTypePart#isDerived()} property.
	 */
	void setDerived(boolean value);

}

