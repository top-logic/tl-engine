/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.TLID;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;

/**
 * {@link BranchIdType} representing a {@link TLModule}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Module extends BranchIdType {

	/**
	 * Name of the represented {@link TLModule}.
	 */
	String getModuleName();

	/**
	 * Setter for {@link #getModuleName()}.
	 */
	void setModuleName(String moduleName);

	/**
	 * Internal {@link TLModel#tId() id} of the model of the module.
	 */
	TLID getModel();

	/**
	 * Setter for {@link #getModel()}.
	 */
	void setModel(TLID id);

}

