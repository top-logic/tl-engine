/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.format.TLObjectFormat;
import com.top_logic.model.search.ui.model.misc.Multiplicity;
import com.top_logic.model.search.ui.model.options.AllTypes;

/**
 * Common properties for "all of type" searches.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractTypeSearch extends FilterContainer, SubSearch {

	/**
	 * Property name of {@link #getType()}.
	 */
	String TYPE = "type";

	/**
	 * The type of objects being searched.
	 */
	@Name(TYPE)
	@Mandatory
	@Format(TLObjectFormat.class)
	@Options(fun = AllTypes.class, args = @Ref(CONFIG_NAME))
	TLClass getType();

	@Override
	@DerivedRef(TYPE)
	TLType getValueType();

	@Override
	@Derived(fun = Multiplicity.SingleValue.class, args = {})
	boolean getValueMultiplicity();

}
