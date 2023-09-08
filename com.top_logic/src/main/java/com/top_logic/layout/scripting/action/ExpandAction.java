/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.ExpandActionOp;

/**
 * {@link ApplicationAction} for toggling the expansion state of a tree node.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface ExpandAction extends ModelAction {

	@Override
	public Class<? extends ExpandActionOp> getImplementationClass();

	/**
	 * The path to the node, without root.
	 */
	ModelName getPath();

	/**
	 * @see #getPath()
	 */
	void setPath(ModelName path);

	/**
	 * true: Expand, false: Collapse
	 */
	boolean isExpand();

	/**
	 * @see #isExpand()
	 */
	void setExpand(boolean expand);

}
