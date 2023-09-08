/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.core;

import java.util.Arrays;
import java.util.Collection;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.client.diagramjs.model.util.Bounds;
import com.top_logic.client.diagramjs.model.util.Position;
import com.top_logic.client.diagramjs.util.JavaScriptObjectUtil;

/**
 * Modeler for {@link Diagram} components.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class Modeler extends JavaScriptObject {

	/**
	 * Creates a {@link Modeler}.
	 */
	protected Modeler() {
		super();
	}

	/**
	 * Reizes the shape to the given new bounds.
	 */
	public final native void resizeShape(Shape shape, Bounds newBounds) /*-{
		this.resizeShape(shape, newBounds);
	}-*/;

	/**
	 * Moves the shape to the given {@link Position}.
	 */
	public final native void moveShape(Shape shape, Position newPosition) /*-{
		this.moveShape(shape, newPosition);
	}-*/;

	/**
	 * @see #removeElements(Collection)
	 */
	public final void removeElement(Base element) {
		removeElements(Arrays.asList(element));
	}

	/**
	 * Remove the given elements from diagram.
	 */
	public final void removeElements(Collection<Base> elements) {
		removeElementsInternal(JavaScriptObjectUtil.getArray(elements));
	}

	private final native void removeElementsInternal(JavaScriptObject elements) /*-{
		this.removeElements(elements);
	}-*/;

}
