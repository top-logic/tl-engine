/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * General graph part options.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public abstract class GraphPartOptions extends JavaScriptObject {

	/**
	 * Creates a {@link GraphPartOptions}.
	 */
	protected GraphPartOptions() {
		super();
	}

	/**
	 * Business object of this {@link Connection}.
	 */
	public final native Object getSharedGraphPart() /*-{
		return this.sharedGraphPart;
	}-*/;

	/**
	 * {@link #getSharedGraphPart()}
	 */
	public final native void setSharedGraphPart(Object sharedGraphPart) /*-{
		this.sharedGraphPart = sharedGraphPart;
	}-*/;

	/**
	 * Returns true if the given graph part is visible.
	 */
	public final native boolean isVisible() /*-{
		return this.isVisible;
	}-*/;

	/**
	 * @see #isVisible()
	 */
	public final native boolean setVisibility(boolean isVisible) /*-{
		this.isVisible = isVisible;
	}-*/;

	/**
	 * Tags of a graph part.
	 */
	public final native String[] getTags() /*-{
		return this.tags;
	}-*/;

	/**
	 * @see #getTags()
	 */
	public final native void setTags(String[] tags) /*-{
		this.tags = tags;
	}-*/;

}
