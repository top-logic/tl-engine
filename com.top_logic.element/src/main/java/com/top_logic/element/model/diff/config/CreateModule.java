/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.model.TLModule;

/**
 * Creation of a new {@link TLModule}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("create-module")
public interface CreateModule extends Create {

	/**
	 * Description of the {@link TLModule} to be created.
	 */
	@Mandatory
	@DefaultContainer
	ModuleConfig getModule();
	
	/** @see #getModule() */
	void setModule(ModuleConfig value);

}
