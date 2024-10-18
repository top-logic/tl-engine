/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.config.PartConfig;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Creation of a {@link TLStructuredTypePart}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("create-part")
public interface CreateStructuredTypePart extends CreatePart {

	/**
	 * Description of the {@link TLStructuredTypePart} to add to {@link #getType()}.
	 */
	@Mandatory
	@DefaultContainer
	PartConfig getPart();
	
	/** @see #getPart() */
	void setPart(PartConfig value);

}
