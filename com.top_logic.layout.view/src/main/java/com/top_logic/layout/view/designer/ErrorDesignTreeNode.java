/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Synthetic design-tree node standing in for a referenced view that could not be loaded.
 *
 * <p>
 * Rendered instead of the referenced view's subtree when a view file named by a
 * {@link com.top_logic.layout.view.ViewRef} property cannot be loaded (e.g. the target
 * {@code .view.xml} is invalid), so the problem is visible in the designer tree rather than silently
 * missing. Carries no {@link com.top_logic.basic.config.ConfigurationItem}, so it is
 * never edited or saved.
 * </p>
 */
public class ErrorDesignTreeNode extends DesignTreeNode {

	private final String _viewName;

	private final String _message;

	/**
	 * Creates an {@link ErrorDesignTreeNode}.
	 *
	 * @param sourceFile
	 *        The .view.xml file that contains the failing reference.
	 * @param viewName
	 *        The referenced view (as written in the referencing property).
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
		return _viewName;
	}

	@Override
	public String getDisplayLabel() {
		// The icon of the row marks the node as an error, see DesignNodeKind#ERROR.
		return Resources.getInstance().getString(I18NConstants.DESIGNER_INVALID_VIEW__PATH.fill(_viewName));
	}

	@Override
	public String getTooltipHtml() {
		return TagUtil.encodeXML(Resources.getInstance()
			.getString(I18NConstants.DESIGNER_INVALID_VIEW__PATH_DETAILS.fill(_viewName, _message)));
	}

}
