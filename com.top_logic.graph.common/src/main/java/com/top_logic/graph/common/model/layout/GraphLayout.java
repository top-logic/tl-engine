/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.layout;

import com.top_logic.basic.config.annotation.Name;

/**
 * Layout algorithm hint.
 * 
 * <p>
 * This is a rudimentary placeholder for a real graph layout configuration.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphLayout {

	/**
	 * Property name of {@link #getOrientation()}.
	 */
	String ORIENTATION = "orientation";

	/**
	 * Preferred orientation in which graph edges are drawn.
	 */
	@Name(ORIENTATION)
	LayoutOrientation getOrientation();

}
