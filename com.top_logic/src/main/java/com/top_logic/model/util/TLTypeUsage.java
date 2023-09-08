/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.Collection;

import com.top_logic.model.TLQuery;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;

/**
 * {@link TLQuery} computing the usage of a {@link TLType} in {@link TLTypePart#getType()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLTypeUsage extends TLQuery {

	/**
	 * All {@link TLStructuredTypePart}s that use the given type in {@link TLTypePart#getType()}.
	 */
	Collection<TLStructuredTypePart> getUsage(TLType targetType);

}
