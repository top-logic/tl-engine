/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.AssertionTreeNode;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.value.TreeNodeSelectionRef;

/**
 * {@link TreeNodeAssertionPlugin} for the selection of a node.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeNodeSelectionAssertionPlugin<N> extends TreeNodeAssertionPlugin<N, BooleanField> {

	/**
	 * Creates a new {@link TreeNodeSelectionAssertionPlugin}.
	 */
	public TreeNodeSelectionAssertionPlugin(AssertionTreeNode<N> model) {
		super(model, "nodeSelection");
	}

	@Override
	protected BooleanField createValueField(String name) {
		BooleanField valueField = FormFactory.newBooleanField(name);
		return valueField;
	}

	@Override
	protected Object getInitialValue() {
		return Boolean.valueOf(getModel().isSelected());
	}

	@Override
	protected GuiAssertion buildAssertion() {
		ModelName expected = ReferenceInstantiator.booleanValue(getExpectedValueField().getAsBoolean());
		ModelName actualValue = ReferenceInstantiator.treeNodeRef(getModel(), TreeNodeSelectionRef.class);
		return ActionFactory.valueAssertion(expected, actualValue, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.NODE_SELECTION;
	}
}
