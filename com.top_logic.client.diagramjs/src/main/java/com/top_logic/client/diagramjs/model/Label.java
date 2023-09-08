/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

/**
 * Represents diagram text.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class Label extends Shape {

	/**
	 * Creates a {@link Label}.
	 */
	protected Label() {
		super();
	}

	/**
	 * Owner of this label.
	 */
	public final native Base getOwner() /*-{
		return this.labelTarget;
	}-*/;

	/**
	 * Type of this label.
	 */
	public final native String getType() /*-{
		return this.labelType;
	}-*/;

}
