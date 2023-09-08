/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The basic graphical representation of a diagram component.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class Base extends JavaScriptObject {

	/**
	 * Creates a {@link Base}.
	 */
	protected Base() {
		super();
	}

	/**
	 * The object that backs up the shape.
	 */
	public final native JavaScriptObject getBusinessObject() /*-{
		return this.businessObject;
	}-*/;

	/**
	 * The corresponding shared graph part of this diagramJS component.
	 */
	public final native Object getSharedGraphPart() /*-{
		return this.sharedGraphPart;
	}-*/;

	/**
	 * @see Shape
	 */
	public final native Shape getParent() /*-{
		return this.parent;
	}-*/;

	/**
	 * Connected labels to this base form.
	 */
	public final native Label[] getLabels() /*-{
		return this.labels;
	}-*/;
}
