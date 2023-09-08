/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Changing the oder of {@link TLStructuredTypePart}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("move-part")
public interface MoveStructuredTypePart extends Update, PartReference {

	/**
	 * Description of the {@link TLStructuredTypePart} to move within the properties of its type.
	 */
	@Mandatory
	String getPart();
	
	/** @see #getPart() */
	void setPart(String value);

}
