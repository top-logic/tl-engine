/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;

import com.top_logic.basic.Named;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.common.folder.AbstractFolderTreeBuilder;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.knowledge.wrap.AbstractContainerWrapper;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.tree.model.TreeBuilder;

/**
 * {@link TreeBuilder} for {@link AbstractContainerWrapper}/{@link WebFolder}
 * object hierarchies.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebFolderTreeBuilder<C extends WebFolderTreeBuilder.Config<?>> extends AbstractFolderTreeBuilder<C> {

	/** {@link ConfigurationItem} for the {@link WebFolderTreeBuilder}. */
	public interface Config<T extends WebFolderTreeBuilder<?>> extends AbstractFolderTreeBuilder.Config<T> {
		// nothing needed, yet.
	}

	/** The default {@link WebFolderTreeBuilder}. */
	@SuppressWarnings("unchecked")
	// Note: Explicit cast is required for certain Java compilers.
	public static final WebFolderTreeBuilder<?> INSTANCE =
		(WebFolderTreeBuilder<?>) TypedConfigUtil.createInstance(Config.class);

	/** {@link TypedConfiguration} constructor for {@link WebFolderTreeBuilder}. */
	public WebFolderTreeBuilder(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected boolean isLink(FolderNode parent, Object userObject) {
		boolean isLink =
				(parent != null) &&
				(parent.getBusinessObject() instanceof WebFolder) &&
				((WebFolder) parent.getBusinessObject()).isLinkedContent((Named) userObject);
		return isLink;
	}


}
