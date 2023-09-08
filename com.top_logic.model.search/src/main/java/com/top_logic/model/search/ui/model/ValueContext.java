/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * {@link SearchPart} that in some way produces values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface ValueContext extends SearchPart {

	/** Constant for {@link #getValueMultiplicity()}. */
	boolean SINGLE_VALUE = false;

	/** Constant for {@link #getValueMultiplicity()}. */
	boolean COLLECTION_VALUE = true;

	/**
	 * Property name of {@link #getValueType()}.
	 */
	String VALUE_TYPE = "value-type";

	/**
	 * Property name of {@link #getValueMultiplicity()}.
	 */
	String VALUE_MULTIPLICITY = "value-multiplicity";

	/**
	 * The {@link TLType} that this expression is about.
	 */
	@Abstract
	@Name(VALUE_TYPE)
	TLType getValueType();

	/**
	 * The multiplicity of the {@link #getValueType() value type}.
	 * <p>
	 * false: Only a single value. true: Multiple values.
	 * </p>
	 */
	@Hidden
	@Abstract
	@Name(VALUE_MULTIPLICITY)
	boolean getValueMultiplicity();

}
