/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles.view;

import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * Represents an ARGB (alpha, red, green, blue) encoded color object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Color {

	/**
	 * Gets the value of the red component of the color.
	 * 
	 * The value is between 0 and 255. A higher value represents a higher part of the color is made
	 * up of this component.
	 */
	int getR();

	/**
	 * @see #getR()
	 */
	void setR(int value);

	/**
	 * Gets the value of the green component of the color.
	 * 
	 * The value is between 0 and 255. A higher value represents a higher part of the color is made
	 * up of this component.
	 */
	int getG();

	/**
	 * @see #getG()
	 */
	void setG(int value);

	/**
	 * Gets the value of the blue component of the color.
	 * 
	 * The value is between 0 and 255. A higher value represents a higher part of the color is made
	 * up of this component.
	 */
	int getB();

	/**
	 * @see #getB()
	 */
	void setB(int value);

	/**
	 * Gets the value of the alpha component of the color.
	 * 
	 * The value is between 0 and 255. If this component is 0, then the color will be transparent.
	 */
	@IntDefault(255)
	int getA();

	/**
	 * @see #getA()
	 */
	void setA(int value);

}
