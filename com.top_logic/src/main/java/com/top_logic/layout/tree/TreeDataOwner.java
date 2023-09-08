/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.layout.scripting.recorder.ref.NamedModel;

/**
 * This class is used by the scripting framework to find the owner of the {@link TreeData}. The
 * owner is needed to retrieve/find the {@link TreeData} when the script is executed.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TreeDataOwner extends NamedModel {

	/**
	 * Getter for the owned {@link TreeData}.
	 */
	public TreeData getTreeData();

}
