/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model.util;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.model.Connection;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.client.diagramjs.util.DiagramJSObjectUtil;

/**
 * DiagramJS {@link Connection} context.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ConnectionContext extends JavaScriptObject {

	/**
	 * Creates the {@link ConnectionContext}.
	 */
	protected ConnectionContext() {
		super();
	}

	/**
	 * Connection {@link Shape} source.
	 */
	public final native Shape getSource() /*-{
		return this.source;
	}-*/;

	/**
	 * Connection {@link Shape} target.
	 */
	public final Shape getTarget() {
		return DiagramJSObjectUtil.getNonLabelElement(getTargetInternal()).cast();
	}

	private final native Base getTargetInternal() /*-{
		return this.target;
	}-*/;

	/**
	 * Type of the {@link Connection}.
	 */
	public final native String getType() /*-{
		return this.type;
	}-*/;
}
