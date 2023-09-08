/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * Factory to create {@link SharedObject}s in an application's {@link ObjectScope}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface HandleFactory {

	/**
	 * Creates a new {@link ObjectData} managing the instance state of the newly created
	 * {@link SharedObject}.
	 * 
	 * @param typeName
	 *        The network type name for the resulting object, see
	 *        {@link TypeNaming#networkType(ObjectData)}.
	 * @param scope
	 *        The {@link ObjectScope} to create the underlying {@link ObjectData} in.
	 * @return The {@link ObjectData} container for the newly created object.
	 */
	ObjectData createHandle(String typeName, ObjectScope scope);

}
