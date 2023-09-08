/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.Map;

/**
 * A {@link TemplateNode} that has a {@link Map} of {@link #getAttributes() attributes}.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public abstract class FunctionStatement extends TemplateNode {

	/**
	 * Getter for the {@link Map} of attributes.
	 */
	public abstract Map<String, String> getAttributes();

}