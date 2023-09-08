/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.gui.AssertionTreeNode;

/**
 * Assertion concerning tree nodes.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TreeNodeAssertionPlugin<N, S extends FormField> extends
		SingleValueAssertionPlugin<AssertionTreeNode<N>, S> {

	/**
	 * Creates a new {@link TreeNodeAssertionPlugin}.
	 */
	public TreeNodeAssertionPlugin(AssertionTreeNode<N> model, String internalName) {
		super(model, false, internalName);
	}

}
