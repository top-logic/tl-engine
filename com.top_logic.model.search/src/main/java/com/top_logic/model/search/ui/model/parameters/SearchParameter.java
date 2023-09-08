/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.parameters;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.NamedDefinition;
import com.top_logic.model.search.ui.model.Search;

/**
 * Definition of a parameter for a {@link Search} query.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@Abstract
public interface SearchParameter extends NamedDefinition {

	@Abstract
	@Override
	String getName();

	/** @see #getValueType() */
	void setValueType(TLType type);

	/** @see #getValueMultiplicity() */
	void setValueMultiplicity(boolean valueMultiplicity);

}
