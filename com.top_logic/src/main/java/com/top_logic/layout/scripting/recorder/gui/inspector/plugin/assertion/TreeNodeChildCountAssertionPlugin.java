/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.AssertionTreeNode;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.value.TreeNodeChildCountRef;

/**
 * {@link TreeNodeAssertionPlugin} for the number of children of a tree node.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeNodeChildCountAssertionPlugin<N> extends TreeNodeAssertionPlugin<N, IntField> {

	/**
	 * Creates a new {@link TreeNodeChildCountAssertionPlugin}.
	 */
	public TreeNodeChildCountAssertionPlugin(AssertionTreeNode<N> model) {
		super(model, "childCount");
	}

	@Override
	protected IntField createValueField(String name) {
		IntField valueField = FormFactory.newIntField(name);
		return valueField;
	}

	@Override
	protected Object getInitialValue() {
		return Integer.valueOf(getModel().treeModel().getChildren(getModel().getNode()).size());
	}

	@Override
	protected GuiAssertion buildAssertion() {
		ModelName expected = ReferenceInstantiator.intValue(getExpectedValueField().getAsInteger().intValue());
		ModelName actualValue = ReferenceInstantiator.treeNodeRef(getModel(), TreeNodeChildCountRef.class);
		return ActionFactory.valueAssertion(expected, actualValue, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.CHILD_COUNT;
	}

}
