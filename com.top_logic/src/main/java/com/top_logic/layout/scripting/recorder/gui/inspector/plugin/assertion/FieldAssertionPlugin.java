/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The gui elements for an assertion the tester can make about a {@link FormField}. <br/>
 * Use {@link #registerFormMembers(FormContainer)} to add it to an ui. <br/>
 * Use {@link #recordAssertionIfRequested()} to record the assertion.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class FieldAssertionPlugin<M extends FormMember, S extends FormField> extends
		SingleValueAssertionPlugin<M, S> {

	/**
	 * After creating the {@link FieldAssertionPlugin}, call {@link #recordAssertionIfRequested()} to
	 * add it to the gui.
	 */
	public FieldAssertionPlugin(M model, boolean assertByDefault, String internalName) {
		super(model, assertByDefault, internalName);
	}

	@Override
	protected final GuiAssertion buildAssertion() {
		ModelName fieldModelName = getModel().getModelName();
		return buildAssertion(fieldModelName);
	}

	protected abstract GuiAssertion buildAssertion(ModelName formMemberName);

}
