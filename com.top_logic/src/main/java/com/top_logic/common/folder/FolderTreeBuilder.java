/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.common.folder.model.FolderNode;

/**
 * TreeBuilder to wrap a FolderDefinition in a tree.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class FolderTreeBuilder<C extends FolderTreeBuilder.Config<?>> extends AbstractFolderTreeBuilder<C> {

	/** {@link ConfigurationItem} for the {@link FolderTreeBuilder}. */
	public interface Config<T extends FolderTreeBuilder<?>> extends AbstractFolderTreeBuilder.Config<T> {
		// nothing needed, yet.
	}

	/** The default {@link FolderTreeBuilder}. */
	@SuppressWarnings("unchecked")
	// Note: Explicit cast is required for certain Java compilers.
	public static final FolderTreeBuilder<?> INSTANCE =
		(FolderTreeBuilder<?>) TypedConfigUtil.createInstance(FolderTreeBuilder.Config.class);

	/** {@link TypedConfiguration} constructor for {@link FolderTreeBuilder}. */
	public FolderTreeBuilder(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected boolean isLink(FolderNode parent, Object userObject) {
		return false;
	}

}
