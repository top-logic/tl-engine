/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles;

import com.top_logic.basic.config.annotation.Name;

/**
 * A {@link NodeStyle} that decorates an inner {@link NodeStyle}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface DecoratorNodeStyle extends NodeStyle {

	/** Property name of {@link #getInnerStyle()}. */
	String INNER_STYLE = "innerStyle";

	/** The {@link NodeStyle} which will be decorated. */
	@Name(INNER_STYLE)
	NodeStyle getInnerStyle();

}
