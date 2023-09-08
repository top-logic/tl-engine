/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TypeConfig;

/**
 * Creation of a new {@link TLType}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("create-type")
public interface CreateType extends Create, ModulePartDiff {

	/**
	 * Description of the {@link TLType} to create in {@link #getModule()}.
	 */
	@Mandatory
	@DefaultContainer
	TypeConfig getType();
	
	/** @see #getType() */
	void setType(TypeConfig value);

}
