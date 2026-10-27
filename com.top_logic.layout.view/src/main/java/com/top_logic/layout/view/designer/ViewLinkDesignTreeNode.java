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
 * Design-tree leaf standing in for a referenced view that is expanded at another place of the tree.
 *
 * <p>
 * {@link DesignTreeBuilder} expands each view file only once. Every further reference to it is
 * shown as this node, which names the referenced file instead of repeating its content. This also
 * terminates reference cycles. Selecting the node selects the expanded view instead, see
 * {@link #getSelectionTarget()}. The node carries no
 * {@link com.top_logic.basic.config.ConfigurationItem}, so it is never edited or saved.
 * </p>
 */
public class ViewLinkDesignTreeNode extends DesignTreeNode {

	/** Arrow (U+2192) prefixed to the label to mark the node as a link. */
	private static final String ARROW = "→";

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
	 * which is the {@link DesignTreeNode#getSourceFile() source file} of the nodes the view is
	 * expanded to.
	 */
	public String getTargetFile() {
		return _target.fullPath();
	}

	/**
	 * The node the {@link #getTargetFile() referenced view} is expanded to, or this node itself if the
	 * tree does not show that view.
	 */
	@Override
	public DesignTreeNode getSelectionTarget() {
		DesignTreeNode target = SelectViewCommand.findViewRoot(getRoot(), getTargetFile());
		return target != null ? target : this;
	}

	@Override
	public String getTagName() {
		return _target.getPath();
	}

	@Override
	public String getDisplayLabel() {
		return ARROW + " " + _target.getPath();
	}

	@Override
	public String getTooltipHtml() {
		return TagUtil.encodeXML(
			Resources.getInstance().getString(I18NConstants.DESIGNER_VIEW_LINK__PATH.fill(_target.getPath())));
	}
}
