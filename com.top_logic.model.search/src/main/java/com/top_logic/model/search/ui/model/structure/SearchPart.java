/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.structure;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.model.search.ModelBasedSearch;

/**
 * Base model for search UIs.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface SearchPart extends ConfigPart {

	/**
	 * Property name of {@link #getContext()}.
	 */
	String CONTEXT = "context";

	/** Property name of {@link #getConfigName()}. */
	String CONFIG_NAME = "config-name";

	/**
	 * The parent {@link SearchPart} this {@link SearchPart} lives in.
	 */
	@Name(CONTEXT)
	@Container
	@Hidden
	SearchPart getContext();

	/**
	 * The name of the configuration of this {@link SearchPart} tree.
	 * 
	 * @see ModelBasedSearch#getSearchConfig(String) Retrieve the corresponding configuration.
	 */
	@DerivedRef({ CONTEXT, CONFIG_NAME })
	@Hidden
	@Name(CONFIG_NAME)
	String getConfigName();

}
