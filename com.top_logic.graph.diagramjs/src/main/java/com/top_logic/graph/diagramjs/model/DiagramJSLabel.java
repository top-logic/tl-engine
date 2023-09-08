/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.model;

import com.top_logic.graph.common.model.Label;

/**
 * An diagramjs label.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface DiagramJSLabel extends Label {

	/**
	 * Name of {@link #getType()} property.
	 */
	public String LABEL_TYPE = "labelType";
	
	/**
	 * Type of this {@link DiagramJSLabel}.
	 */
	String getType();

	/**
	 * @see #getType()
	 */
	void setType(String type);
}
