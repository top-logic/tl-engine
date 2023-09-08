/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.TemplateVariable;

/**
 * A view is a visible thing that can can render itself.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface View extends HTMLFragment {

	/**
	 * Whether this view should be displayed at all.
	 * 
	 * @implNote If <code>false</code>, {@link #write(DisplayContext, TagWriter)} should not produce
	 *           any user-visible output.
	 */
	@TemplateVariable("visible")
	public boolean isVisible();

}
