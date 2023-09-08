/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The {@link TreeDataName} consists of a {@link ModelName} for the {@link TreeDataOwner} of the
 * {@link TreeData}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TreeDataName extends ModelName {

	ModelName getTreeDataOwner();

	void setTreeDataOwner(ModelName treeDataOwner);

}
