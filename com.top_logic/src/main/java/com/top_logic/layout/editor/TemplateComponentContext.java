/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Template context for {@link LayoutComponent}'s instantiated by a typed template.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface TemplateComponentContext extends TemplateContext {

	/**
	 * Name of the component which is instantiated from the given template.
	 */
	public ComponentName getName();

	/**
	 * @see #getName()
	 */
	public void setName(ComponentName name);

}
