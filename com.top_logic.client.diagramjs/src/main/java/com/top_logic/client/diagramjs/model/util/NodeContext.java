/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model.util;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * DiagramJS node context.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NodeContext extends JavaScriptObject {

	/**
	 * Creates the {@link NodeContext}.
	 */
	protected NodeContext() {
		super();
	}

	/**
	 * @see Position
	 */
	public final native Position getPosition() /*-{
		return this.position;
	}-*/;

	/**
	 * @see Dimension
	 */
	public final native Dimension getDimension() /*-{
		return this.dimension;
	}-*/;

}
