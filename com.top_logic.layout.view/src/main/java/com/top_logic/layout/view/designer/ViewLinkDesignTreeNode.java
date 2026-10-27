/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewRef;
import com.top_logic.util.Resources;

/**
 * Design-tree leaf standing for a view file another view refers to.
 *
 * <p>
 * A design tree shows the content of a single view file, see {@link DesignTreeBuilder}. A
 * reference to another view file is shown as this node, which is labelled with the path of the
 * referenced file and marked as a link by its icon, see {@link DesignNodeKind#LINK}. Selecting
 * the node opens that file in the designer, see {@link DesignSession#open(String)}. The node
 * carries no {@link com.top_logic.basic.config.ConfigurationItem}, so it is never edited or saved.
 * </p>
 */
public class ViewLinkDesignTreeNode extends DesignTreeNode {

	private final ViewRef _target;

	/**
	 * Creates a {@link ViewLinkDesignTreeNode}.
	 *
	 * @param sourceFile
	 *        The .view.xml file that contains the reference.
	 * @param target
	 *        The referenced view.
	 */
	public ViewLinkDesignTreeNode(String sourceFile, ViewRef target) {
		super(sourceFile);
		_target = target;
	}

	/**
	 * The referenced view.
	 */
	public ViewRef getTarget() {
		return _target;
	}

	/**
	 * The full path of the referenced view file (e.g. {@code /WEB-INF/views/customers/detail.view.xml}),
	 * which is the {@link DesignTreeNode#getSourceFile() source file} of the nodes of its tree.
	 */
	public String getTargetFile() {
		return _target.fullPath();
	}

	@Override
	public String getTagName() {
		return _target.getPath();
	}

	@Override
	public String getTooltipHtml() {
		return TagUtil.encodeXML(
			Resources.getInstance().getString(I18NConstants.DESIGNER_VIEW_LINK__PATH.fill(_target.getPath())));
	}
}
