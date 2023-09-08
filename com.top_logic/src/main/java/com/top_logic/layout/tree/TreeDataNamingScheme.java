/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * The {@link ModelNamingScheme} for {@link TreeData} instances relies on the {@link TreeDataOwner}
 * retrievable via {@link TreeData#getOwner()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TreeDataNamingScheme extends AbstractModelNamingScheme<TreeData, TreeDataName> {

	/**
	 * The instance of the {@link TreeDataNamingScheme}. This is not a
	 * singleton, as (potential) subclasses can create further instances.
	 */
	public static final TreeDataNamingScheme INSTANCE = new TreeDataNamingScheme();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #INSTANCE}
	 * constant directly.
	 */
	protected TreeDataNamingScheme() {
		// See JavaDoc above.
	}

	@Override
	public Class<TreeDataName> getNameClass() {
		return TreeDataName.class;
	}

	@Override
	public Class<TreeData> getModelClass() {
		return TreeData.class;
	}

	@Override
	public TreeData locateModel(ActionContext context, TreeDataName name) {
		TreeDataOwner treeDataOwner = (TreeDataOwner) ModelResolver.locateModel(context, name.getTreeDataOwner());
		return treeDataOwner.getTreeData();
	}

	@Override
	protected void initName(TreeDataName name, TreeData model) {
		// Don't check whether the owner exists. isCompatibleModel(TreeData) checked that already.
		name.setTreeDataOwner(model.getOwner().get().getModelName());
	}

	@Override
	protected boolean isCompatibleModel(TreeData model) {
		return model.getOwner().hasValue();
	}

}
