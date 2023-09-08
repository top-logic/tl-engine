/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.model;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.Named;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.folder.FolderTreeBuilder;
import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;

/**
 * {@link AbstractMutableTLTreeNode} created by {@link FolderTreeBuilder}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FolderNode extends AbstractMutableTLTreeNode<FolderNode> implements FolderContent {

	private final boolean isLink;

	/**
	 * Creates a {@link FolderNode}.
	 * 
	 * @param isLink
	 *        See {@link #isLink()}.
	 */
	public FolderNode(AbstractMutableTLTreeModel<FolderNode> model, FolderNode parent, Named userObject, boolean isLink) {
		super(model, parent, userObject);
		assert userObject != null : "User object in a folder tree must not be null.";
		this.isLink = isLink;
	}

	@Override
	public final boolean isLink() {
		return isLink;
	}

	public boolean isFolder() {
		return getBusinessObject() instanceof FolderDefinition;
	}

	@Override
	public final Object getFolder() {
		FolderNode parentNode = getParent();
		if (parentNode == null) {
			return null;
		}
		return parentNode.getBusinessObject();
	}

	@Override
	public final Object getContent() {
		return getBusinessObject();
	}

	/**
	 * {@link ValueNamingScheme} for {@link FolderNode}s.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Naming extends ValueNamingScheme<FolderNode> {

		@Override
		public Class<FolderNode> getModelClass() {
			return FolderNode.class;
		}

		@Override
		public Map<String, Object> getName(FolderNode model) {
			Object businessObject = model.getBusinessObject();
			return Collections.<String, Object> singletonMap("element", ((Named) businessObject).getName());
		}

		@Override
		public boolean matches(Map<String, Object> name, FolderNode model) {
			Object businessObject = model.getBusinessObject();
			return businessObject instanceof Named &&
				name.get("element").equals(((Named) businessObject).getName());
		}

	}

}
