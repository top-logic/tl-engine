/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * Strategy for assigning/creating {@link ObjectData} adaptors for {@link SharedObject}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DataMapping {

	/**
	 * Create an {@link ObjectData} for the given shared object.
	 * 
	 * @param scope
	 *        The {@link ObjectScope} to use.
	 * @param obj
	 *        The {@link SharedObject} or generalized shared object to create a data adaptor for.
	 * @return A new {@link ObjectData} to for the given shared object.
	 */
	ObjectData data(ObjectScope scope, Object obj);

}
