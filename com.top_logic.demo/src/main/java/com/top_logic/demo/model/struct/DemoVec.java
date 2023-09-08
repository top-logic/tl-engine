/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.struct;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * Demo for a structured configuration type as part of a persistent model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DemoVec extends ConfigurationItem {

	/**
	 * The X coordinate.
	 */
	float getX();

	/**
	 * The Y coordinate.
	 */
	float getY();

}
