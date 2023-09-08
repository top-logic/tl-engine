/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Named;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.TreeBuilder;

/**
 * {@link TreeBuilder} for {@link FolderNode}.
 *
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractFolderTreeBuilder<C extends AbstractFolderTreeBuilder.Config<?>>
		extends AbstractConfiguredInstance<C>
		implements TreeBuilder<FolderNode> {

	/** {@link ConfigurationItem} for the {@link AbstractFolderTreeBuilder}. */
	public interface Config<T extends AbstractFolderTreeBuilder<?>> extends PolymorphicConfiguration<T> {

		/** Property name of {@link #getFileFilter()}. */
		String FILE_FILTER = "fileFilter";

		/**
		 * Only files accepted by this filter will are added to the tree.
		 * <p>
		 * Folders are not filtered, only files.
		 * </p>
		 */
		@Name(FILE_FILTER)
		PolymorphicConfiguration<Filter<Named>> getFileFilter();

	}

	private final Filter<Named> _fileFilter;

	/** {@link TypedConfiguration} constructor for {@link AbstractFolderTreeBuilder}. */
	public AbstractFolderTreeBuilder(InstantiationContext context, C config) {
		super(context, config);
		_fileFilter = context.getInstance(config.getFileFilter());
	}

	@Override
	public FolderNode createNode(AbstractMutableTLTreeModel<FolderNode> model, FolderNode parent, Object userObject) {

		boolean isLink = isLink(parent, userObject);

		return new FolderNode(model, parent, (Named) userObject, isLink);
	}

	/**
	 * @param parent
	 *        the parent of the node of userObject
	 * @param userObject
	 *        the object to examine
	 * @return is the userObject a link in parent?
	 */
	protected abstract boolean isLink(FolderNode parent, Object userObject);

	@Override
	public List<FolderNode> createChildList(FolderNode node) {
		if (!node.isFolder()) {
			return Collections.emptyList();
		}

		FolderDefinition folder = (FolderDefinition) node.getBusinessObject();

		if (folder instanceof Wrapper) {
			if (!((Wrapper) folder).tValid()) {
				return Collections.emptyList();
			}
		}

		Collection<? extends Named> contents = folder.getContents();
		ArrayList<FolderNode> result = new ArrayList<>(contents.size());
		for (Named content : contents) {
			if (isAccepted(content)) {
				result.add(createNode(node.getModel(), node, content));
			}
		}
		return result;

	}

	private boolean isAccepted(Named content) {
		if (_fileFilter == null) {
			return true;
		}
		if (content instanceof FolderDefinition) {
			/* Don't filter folders, only files. */
			return true;
		}
		return _fileFilter.accept(content);
	}

	@Override
	public boolean isFinite() {
		return false;
	}

}
