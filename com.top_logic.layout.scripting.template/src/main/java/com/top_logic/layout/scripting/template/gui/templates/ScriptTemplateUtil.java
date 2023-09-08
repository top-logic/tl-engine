/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import com.top_logic.layout.scripting.template.gui.templates.node.TemplateLocation;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel.TreeUINode;
import com.top_logic.layout.tree.model.StructureTreeModel.Node;

/**
 * Utilities for ScriptRecorder templates.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ScriptTemplateUtil {

	/**
	 * The configured template root path for the given template component.
	 */
	public static String getTemplateRootPath(TemplateTreeComponent templateComponent) {
		return getTemplateTreeBuilder(templateComponent).getTemplateRootResourcePath();
	}

	private static TemplateTreeBuilder getTemplateTreeBuilder(TemplateTreeComponent templateComponent) {
		return (TemplateTreeBuilder) templateComponent.getBuilder();
	}

	/**
	 * Full qualified path for the selected ScriptRecorder template.
	 */
	public static String getSelectedTemplatePath(TemplateTreeComponent templateComponent) {
		return ScriptTemplateUtil.getTemplateRootPath(templateComponent) + getSelectedTemplateResourcePathSuffix(templateComponent);
	}

	/**
	 * Relative path to the template root for the selected ScriptRecorder template.
	 */
	public static String getSelectedTemplateResourcePathSuffix(TemplateTreeComponent templateComponent) {
		return getSelectedTemplateLocation(templateComponent).getResourceSuffix();
	}

	/**
	 * {@link TemplateLocation} for the selected template in the given template component.
	 */
	public static TemplateLocation getSelectedTemplateLocation(TemplateTreeComponent component) {
		return (TemplateLocation) getSelectedNode(component).getBusinessObject();
	}

	private static TreeUINode<?> getRoot(TreeComponent component) {
		return (TreeUINode<?>) component.getTreeData().getTreeModel().getRoot();
	}

	/**
	 * Returns the selected node of the given {@link TreeComponent}. If no object is selected the
	 * root node is returned.
	 */
	public static Node getSelectedNode(TreeComponent component) {
		Object selectedTreeNode = component.getSelected();

		if (selectedTreeNode == null) {
			selectedTreeNode = getRoot(component).getBusinessObject();
		}

		return (Node) selectedTreeNode;
	}

	/**
	 * ScriptRecorder template name of the selected template in the given template
	 *         component.
	 */
	public static String getSelectedTemplateName(TemplateTreeComponent templateComponent) {
		return getSelectedTemplateLocation(templateComponent).getName();
	}
}
