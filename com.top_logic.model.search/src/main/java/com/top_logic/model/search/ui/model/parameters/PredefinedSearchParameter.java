/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.parameters;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.format.IgnoredFormat;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.TranslatedSearchPart;

/**
 * A {@link SearchParameter} that is not defined by the user but built-in.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface PredefinedSearchParameter extends SearchParameter, TranslatedSearchPart {

	/** Property name of {@link #getValue()}. */
	String VALUE = "value";

	@Hidden
	@Override
	String getName();

	@Override
	@ReadOnly
	TLType getValueType();

	@Override
	@ReadOnly
	boolean getValueMultiplicity();

	/** The value of the parameter. */
	@Name(VALUE)
	@ReadOnly
	@Format(IgnoredFormat.class)
	Object getValue();

	/** @see #getValue() */
	void setValue(Object value);

}
