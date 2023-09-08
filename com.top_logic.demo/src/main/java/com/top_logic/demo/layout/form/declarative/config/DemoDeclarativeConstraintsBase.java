/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;

/**
 * Demo for property with inherited constraint.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface DemoDeclarativeConstraintsBase extends ConfigurationItem {

	/**
	 * @see #getOnlyLetters()
	 */
	String ONLY_LETTERS = "only-letters";

	/**
	 * A string containing only letters from A to Z.
	 */
	@Name(ONLY_LETTERS)
	@RegexpConstraint(value = "[a-zA-Z]*")
	String getOnlyLetters();

}
