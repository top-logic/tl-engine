/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

import java.util.Arrays;
import java.util.List;

/**
 * Options for a {@link Shape}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class ShapeOptions extends GraphPartOptions {

	/**
	 * Creates a {@link Shape}.
	 */
	protected ShapeOptions() {
		super();
	}

	/**
	 * Modifiers of a class.
	 */
	public List<String> getModifiers() {
		return Arrays.asList(getModifiersInternal());
	}

	private native String[] getModifiersInternal() /*-{
		return this.modifiers;
	}-*/;

	/**
	 * Stereotypes of a class.
	 */
	public List<String> getStereotypes() {
		return Arrays.asList(getStereotypesInternal());
	}

	private native String[] getStereotypesInternal() /*-{
		return this.stereotypes;
	}-*/;

	/**
	 * True if the class is imported i.e. defined outside this model.
	 */
	public native void setImported(boolean isImported) /*-{
		this.isImported = isImported;
	}-*/;

	/**
	 * @see #getStereotypes()
	 */
	public native void setStereotypes(String[] stereotypes) /*-{
		this.stereotypes = stereotypes;
	}-*/;

	/**
	 * @see #getModifiers()
	 */
	public native void setModifiers(List<String> modifiers) /*-{
		this.modifiers = modifiers;
	}-*/;

}
