/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.tree.renderer.NoResourceProvider;

/**
 * Base class for implementing the {@link ResourceProvider} interface.
 * 
 * <p>
 * To make in instance of this class useful, at least one method must be overridden.
 * </p>
 * 
 * @see NoResourceProvider The only implementation that overrides no methods to hide the displayed
 *      object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractResourceProvider implements ResourceProvider {

	@Override
	public String getLabel(Object object) {
		return null;
	}

	@Override
	public String getType(Object anObject) {
		return null;
	}

	@Override
	public String getTooltip(Object anObject) {
		return null;
	}

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		return null;
	}

	@Override
	public String getLink(DisplayContext context, Object anObject) {
		return null;
	}

	@Override
	public String getCssClass(Object anObject) {
		return null;
	}

}
