/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.migration.data;

import com.top_logic.basic.TLID;
import com.top_logic.model.TLTypePart;

/**
 * {@link BranchIdType} representing a {@link TLTypePart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TypePart extends BranchIdType {

	/**
	 * Name of the represented {@link TLTypePart}.
	 */
	String getPartName();

	/**
	 * Setter for {@link #getPartName()}.
	 */
	void setPartName(String name);

	/**
	 * Owner {@link Type} of the represented {@link TLTypePart}.
	 */
	Type getOwner();

	/**
	 * Setter for {@link #getOwner()}.
	 */
	void setOwner(Type type);

	/**
	 * ID of the definition for this {@link TypePart}.
	 */
	TLID getDefinition();

	/**
	 * Setter for {@link #getDefinition()}.
	 */
	void setDefinition(TLID definition);

}

