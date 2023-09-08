/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.equal.EqualityByValue;

/**
 * Representation of a database version in <i>TopLogic</i>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Version extends NamedConfigMandatory, EqualityByValue {

	/** Configuration name of value of {@link #getModule()} */
	String MODULE_ATTRIBUTE = "module";

	/**
	 * Name of a module which requires a database version.
	 */
	@Name(MODULE_ATTRIBUTE)
	String getModule();

	/**
	 * Setter of {@link #getModule()}.
	 * 
	 * @param module
	 *        New value of {@link #getModule()}.
	 */
	void setModule(String module);

	/**
	 * Name of the version in the database.
	 */
	@Override
	String getName();

}

