/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Methods from <code>com.top_logic.element.meta.StorageImplementation</code> that are needed in
 * "com.top_logic".
 * <p>
 * These methods are used by the transient {@link TLModel} which is used by the
 * <code>com.top_logic.element.model.generate.WrapperGenerator</code>.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface StorageDetail {

	/**
	 * Whether this {@link StorageDetail} does not support storing values.
	 * <p>
	 * In a read-only {@link StorageDetail}, modifications are not allowed.
	 * </p>
	 */
	boolean isReadOnly();

}
