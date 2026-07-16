/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

/**
 * Synthetic design-tree node standing in for a referenced view that could not be loaded.
 *
 * <p>
 * Rendered instead of the referenced view's subtree when {@code <view-ref>} resolution fails (e.g.
 * the target {@code .view.xml} is invalid), so the problem is visible in the designer tree rather
 * than silently missing. Carries no {@link com.top_logic.basic.config.ConfigurationItem}, so it is
 * never edited or saved.
 * </p>
 */
public class ErrorDesignTreeNode extends DesignTreeNode {

	/** Warning sign (U+26A0) prefixed to the label to mark the node as an error. */
	private static final String WARNING_SIGN = "\u26A0";

	private final String _viewName;

	private final String _message;

	/**
	 * Creates an {@link ErrorDesignTreeNode}.
	 *
	 * @param sourceFile
	 *        The .view.xml file that contains the failing reference.
	 * @param viewName
	 *        The referenced view (as written in the {@code <view-ref>}).
	 * @param message
	 *        The load error message.
	 */
	public ErrorDesignTreeNode(String sourceFile, String viewName, String message) {
		super(sourceFile);
		_viewName = viewName;
		_message = message == null ? "" : message;
	}

	@Override
	public String getTagName() {
		return "view-ref";
	}

	@Override
	public String getDisplayLabel() {
		return WARNING_SIGN + " Invalid view: " + _viewName;
	}

	@Override
	public String getTooltipHtml() {
		return "Failed to load referenced view <code>" + escape(_viewName) + "</code>: " + escape(_message);
	}

	private static String escape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
