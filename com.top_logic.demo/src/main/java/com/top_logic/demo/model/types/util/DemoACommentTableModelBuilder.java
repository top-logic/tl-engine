/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import com.top_logic.demo.model.types.DemoTypesA;
import com.top_logic.element.comment.layout.CommentTableModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link CommentTableModelBuilder} that only accepts {@link DemoTypesA}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DemoACommentTableModelBuilder extends CommentTableModelBuilder {

	/**
	 * Singleton {@link DemoACommentTableModelBuilder} instance.
	 */
	public static final DemoACommentTableModelBuilder INSTANCE = new DemoACommentTableModelBuilder();

	private DemoACommentTableModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof DemoTypesA && super.supportsModel(aModel, aComponent);
	}

}

