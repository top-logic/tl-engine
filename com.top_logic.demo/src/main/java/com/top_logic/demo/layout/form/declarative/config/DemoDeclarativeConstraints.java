/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.ComparisonDependency;

/**
 * Demo form part with constraints.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DemoDeclarativeConstraints extends DemoDeclarativeConstraintsBase {

	/**
	 * @see #getGreaterThanTen()
	 */
	String GREATER_THAN_TEN = "greater-than-ten";

	/**
	 * @see #getStart()
	 */
	String START = "start";

	/**
	 * @see #getStop()
	 */
	String STOP = "stop";

	/**
	 * Value that must be greater than 10.
	 */
	@Name(GREATER_THAN_TEN)
	@Bound(comparison = Comparision.GREATER, value = 10.0)
	Double getGreaterThanTen();

	/**
	 * A word alphabetically before {@link #getStop()}.
	 */
	@Name(START)
	String getStart();

	/**
	 * A word alphabetically after {@link #getStart()}.
	 */
	@Name(STOP)
	@ComparisonDependency(comparison = Comparision.GREATER_OR_EQUAL, other = @Ref(START), asWarning = true)
	String getStop();

}
