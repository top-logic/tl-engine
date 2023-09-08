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
import com.top_logic.client.diagramjs.util.JavaScriptObjectUtil;

/**
 * Selecter for graph part components of {@link Diagram}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Selection extends JavaScriptObject {

	/**
	 * Creates a {@link Selection}.
	 */
	protected Selection() {
		super();
	}

	/**
	 * Deselect the given element.
	 */
	public final native void deselect(Base element) /*-{
		this.deselect(element);
	}-*/;

	/**
	 * Select the given elements to the already selected elements if #addToSelected is true,
	 * otherwise replace the already selected elements with the given elements.
	 */
	public final void select(Collection<Base> elements, boolean addToSelected) {
		selectInternal(JavaScriptObjectUtil.getArray(elements), addToSelected);
	}

	private final native void selectInternal(JavaScriptObject elements, boolean addToSelected) /*-{
		this.select(elements, addToSelected);
	}-*/;

	private final native Base[] getSelectionInternal() /*-{
		return this.get();
	}-*/;

	/**
	 * The current selected graph elements.
	 */
	public final Collection<Base> getSelection() {
		return Arrays.asList(getSelectionInternal());
	}

	/**
	 * True if no element is selected, otherwise false;
	 */
	public final boolean isEmpty() {
		return getSelection().isEmpty();
	}

}
