/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Denoting a position within the part list of a {@link TLStructuredType}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface PartReference extends ConfigurationItem {

	/**
	 * Local name of the another {@link TLStructuredTypePart} denoting an insert position.
	 * 
	 * <p>
	 * A value of <code>null</code> means the position at the end of the
	 * {@link TLStructuredType#getLocalParts() part list}.
	 * </p>
	 */
	@Nullable
	String getBefore();

	/** @see #getBefore() */
	void setBefore(String value);

}
