/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

/**
 * Options for a {@link Label}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class LabelOptions extends GraphPartOptions {

	/**
	 * Creates a {@link Label}.
	 */
	protected LabelOptions() {
		super();
	}

	/**
	 * Type of this {@link Label}.
	 */
	public native String getType() /*-{
		return this.labelType;
	}-*/;

	/**
	 * {@link #getType()}
	 */
	public native void setType(String type) /*-{
		this.labelType = type;
	}-*/;

}
