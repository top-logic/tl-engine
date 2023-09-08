/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.runtime.action.SelectObjectOp;

/**
 * {@link SelectObjectOp} configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SelectObject extends ComponentAction {

	@Override
	@ClassDefault(SelectObjectOp.class)
	public Class<SelectObjectOp> getImplementationClass();

	PolymorphicConfiguration<Filter<Object>> getMatcherConfig();

	void setMatcherConfig(PolymorphicConfiguration<Filter<Object>> value);
	
}
