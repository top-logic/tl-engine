/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.SubComponentConfig;

/**
 * A sub-layout embedded within another configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface EmbeddedLayoutConfig extends SubComponentConfig {

	/**
	 * Whether to layout {@link #getComponents()} horizontally.
	 * 
	 * <p>
	 * Can be used as short-cut for wrapping the contents with an additional layout.
	 * </p>
	 */
	@Name(Layout.HORIYONTAL_ATT)
	boolean getHorizontal();

}
