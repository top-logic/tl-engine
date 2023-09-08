/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;

/**
 * Base configuration for updates in {@link TLModule} context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface ModulePartDiff extends DiffElement {

	/**
	 * The {@link TLModule} to add the new {@link TLType} to.
	 */
	@Mandatory
	String getModule();

	/** {@link #getModule()} */
	void setModule(String value);

}
