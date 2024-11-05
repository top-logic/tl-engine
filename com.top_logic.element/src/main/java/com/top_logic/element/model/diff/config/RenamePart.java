/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLNamedPart;

/**
 * Renaming of a {@link TLModelPart}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("rename-part")
public interface RenamePart extends Update {

	/**
	 * The qualified name of the {@link TLNamedPart} to rename.
	 */
	@Mandatory
	String getPart();

	/**
	 * @see #getPart()
	 */
	void setPart(String value);

	/**
	 * The new local name of the {@link #getPart() part} after rename.
	 */
	@Mandatory
	String getNewName();

	/**
	 * @see #getNewName()
	 */
	void setNewName(String value);
}
