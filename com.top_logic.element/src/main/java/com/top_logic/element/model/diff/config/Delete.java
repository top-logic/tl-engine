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
 * Deletion of a {@link TLModelPart}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("delete")
public interface Delete extends DiffElement {

	/**
	 * The {@link TLNamedPart#getName() name} of the {@link TLModelPart} to delete.
	 */
	@Mandatory
	String getName();

	/** @see #getName() */
	void setName(String value);

}
