/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Base interface for manipulating the generalization order.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface OrderedGeneralizationUpdate extends GeneralizationUpdate {

	/**
	 * The other generalization to add the {@link #getGeneralization()} before.
	 * 
	 * <p>
	 * A value of <code>null</code> means to append to the list of generalizations.
	 * </p>
	 */
	@Nullable
	String getBefore();

	/** @see #getBefore() */
	void setBefore(String value);

}
