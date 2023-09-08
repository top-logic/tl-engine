/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.annotation.CustomOptionOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.search.ui.model.options.structure.RightHandSideOptions;
import com.top_logic.model.search.ui.model.structure.InheritedContextType;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * Base model for {@link Operator}s that compare object values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface ObjectCompare<I extends Operator.Impl<?>> extends Operator<I>, InheritedContextType {

	/**
	 * Property name for {@link #getCompareObjects()}.
	 */
	String COMPARE_OBJECTS = "compare-objects";

	/**
	 * Description of the right hand side object set to compare with.
	 */
	@Name(COMPARE_OBJECTS)
	@Mandatory
	@CustomOptionOrder
	@Options(fun = RightHandSideOptions.class, args = { @Ref(VALUE_TYPE), @Ref(VALUE_MULTIPLICITY) })
	RightHandSide getCompareObjects();

}
