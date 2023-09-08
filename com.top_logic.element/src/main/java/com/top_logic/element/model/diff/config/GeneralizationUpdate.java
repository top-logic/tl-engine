/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLClass;

/**
 * Base properties for modifying a generalization relation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface GeneralizationUpdate extends ClassUpdate {

	/**
	 * The {@link TLClass} to add or remove to/from the {@link #getType() type's} list of
	 * {@link TLClass#getGeneralizations() generalizations}.
	 */
	@Mandatory
	String getGeneralization();

	/** @see #getGeneralization() */
	void setGeneralization(String value);

}
