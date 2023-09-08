/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

/**
 * Object carrying an application-defined value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TagOwner {

	/**
	 * Property name of {@link #getTag()}.
	 */
	String TAG = "tag";

	/**
	 * User data associated with this object.
	 */
	Object getTag();

	/**
	 * @see #getTag()
	 */
	void setTag(Object value);

}
