/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.component;

import static com.top_logic.basic.col.FilterUtil.*;
import static com.top_logic.layout.tree.model.TLTreeModelUtil.*;

import java.util.List;
import java.util.Objects;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.Utils;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;

/**
 * {@link TreeComponent} implementing {@link DocumentationViewer}.
 * 
 * <p>
 * The {@link DocumentationViewerTree} shows the documentation by searching the tree for the
 * {@link Page} with the given help ID as name.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DocumentationViewerTree extends TreeComponent implements DocumentationViewer {

	/**
	 * Creates a new {@link DocumentationViewerTree}.
	 */
	public DocumentationViewerTree(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public boolean showDocumentation(String helpId) {
		List<DefaultTreeUINode> pages = findPage(helpId);
		if (pages.isEmpty()) {
			/* Not every view has its own documentation page. */
			return false;
		}
		/* There might be several documentation pages with the same helpId, due to misconfiguration.
		 * Open the first one and ignore the rest. The documentation should open, even if a user set
		 * wrong helpIds. */
		return show(pages.get(0));
	}

	private List<DefaultTreeUINode> findPage(String uuid) {
		return filterList(node -> hasUUID(node, uuid), getChildrenRecursively(getTreeModel()));
	}

	private boolean hasUUID(DefaultTreeUINode node, String uuid) {
		if (node == null) {
			return false;
		}
		Object businessObject = node.getBusinessObject();
		if (!(businessObject instanceof Page)) {
			return false;
		}
		Page page = (Page) businessObject;
		return Objects.equals(uuid, page.getUuid());
	}

	private boolean show(DefaultTreeUINode node) {
		Object businessObject = node.getBusinessObject();
		boolean selectionChanged = setSelected(businessObject);
		if (selectionChanged) {
			return true;
		}
		return Utils.equals(getSelected(), businessObject);
	}

}

